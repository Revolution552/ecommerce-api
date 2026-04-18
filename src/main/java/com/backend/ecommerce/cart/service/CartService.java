// src/main/java/com/backend/ecommerce/cart/service/CartService.java
package com.backend.ecommerce.cart.service;

import com.backend.ecommerce.cart.exception.*;
import com.backend.ecommerce.cart.model.Cart;
import com.backend.ecommerce.cart.model.CartItem;
import com.backend.ecommerce.cart.payload.*;
import com.backend.ecommerce.cart.repository.CartItemRepository;
import com.backend.ecommerce.cart.repository.CartRepository;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.model.ProductStatus;
import com.backend.ecommerce.product.repository.ProductRepository;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Value("${cart.expiry.days:7}")
    private int cartExpiryDays;

    @Value("${cart.tax.rate:0.0}")
    private BigDecimal taxRate;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponseDto addToCart(String userEmail, AddToCartRequest request)
            throws ProductNotFoundException, InvalidQuantityException {

        logger.info("Adding product ID: {} to cart for user: {}", request.getProductId(), userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Validate product is available
        validateProductAvailability(product);

        // Validate quantity
        if (request.getQuantity() > product.getAvailableQuantity()) {
            throw new InvalidQuantityException(
                    String.format("Requested quantity (%d) exceeds available stock (%d)",
                            request.getQuantity(), product.getAvailableQuantity()));
        }

        // Get or create cart
        Cart cart = getOrCreateCart(user);

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getAvailableQuantity()) {
                throw new InvalidQuantityException(
                        String.format("Total quantity (%d) exceeds available stock (%d)",
                                newQuantity, product.getAvailableQuantity()));
            }

            cartItem.setQuantity(newQuantity);
            cartItem.setUnitPrice(product.getPrice());
            if (request.getNotes() != null) {
                cartItem.setNotes(request.getNotes());
            }
            if (request.getIsSelected() != null) {
                cartItem.setIsSelected(request.getIsSelected());
            }
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .shop(product.getShop())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .notes(request.getNotes())
                    .isSelected(request.getIsSelected())
                    .build();
            cart.getItems().add(cartItem);
        }

        cartItem.calculateTotalPrice();
        CartItem savedItem = cartItemRepository.save(cartItem);

        cart.updateTotalItems();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Product added to cart successfully. Cart item ID: {}", savedItem.getId());
        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto bulkAddToCart(String userEmail, BulkAddToCartRequest request) {
        logger.info("Bulk adding {} items to cart for user: {}", request.getItems().size(), userEmail);

        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (AddToCartRequest itemRequest : request.getItems()) {
            try {
                addToCart(userEmail, itemRequest);
                successCount++;
            } catch (Exception e) {
                errors.add(String.format("Product %d: %s", itemRequest.getProductId(), e.getMessage()));
            }
        }

        logger.info("Bulk add completed. Success: {}, Errors: {}", successCount, errors.size());

        if (!errors.isEmpty() && successCount == 0) {
            throw new RuntimeException("Failed to add items to cart: " + String.join("; ", errors));
        }

        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto updateCartItem(String userEmail, Long productId, UpdateCartItemRequest request)
            throws CartItemNotFoundException, InvalidQuantityException {

        logger.info("Updating cart item for product ID: {} for user: {}", productId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new CartItemNotFoundException("Product not found in cart"));

        if (request.getQuantity() != null) {
            if (request.getQuantity() == 0) {
                // Remove item if quantity is 0
                cartItemRepository.delete(cartItem);
                cart.getItems().remove(cartItem);
                logger.info("Cart item removed due to zero quantity");
            } else {
                if (request.getQuantity() > product.getAvailableQuantity()) {
                    throw new InvalidQuantityException(
                            String.format("Requested quantity (%d) exceeds available stock (%d)",
                                    request.getQuantity(), product.getAvailableQuantity()));
                }
                cartItem.setQuantity(request.getQuantity());
                cartItem.setUnitPrice(product.getPrice());
            }
        }

        if (cartItem != null) {
            if (request.getNotes() != null) {
                cartItem.setNotes(request.getNotes());
            }
            if (request.getIsSelected() != null) {
                cartItem.setIsSelected(request.getIsSelected());
            }
            cartItemRepository.save(cartItem);
        }

        cart.updateTotalItems();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Cart item updated successfully");
        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto removeFromCart(String userEmail, Long productId) {
        logger.info("Removing product ID: {} from cart for user: {}", productId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

        cart.updateTotalItems();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Product removed from cart successfully");
        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto bulkRemoveFromCart(String userEmail, List<Long> productIds) {
        logger.info("Bulk removing {} items from cart for user: {}", productIds.size(), userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        cartItemRepository.deleteByCartIdAndProductIds(cart.getId(), productIds);

        cart.updateTotalItems();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Bulk remove completed");
        return getCart(userEmail);
    }

    @Transactional
    public void clearCart(String userEmail) {
        logger.info("Clearing cart for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cart.setTotalItems(0);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            logger.info("Cart cleared successfully");
        }
    }

    @Transactional
    public CartResponseDto updateSelection(String userEmail, Long productId, Boolean isSelected)
            throws CartItemNotFoundException {

        logger.info("Updating selection for product ID: {} to {} for user: {}", productId, isSelected, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new CartItemNotFoundException("Product not found in cart"));

        cartItem.setIsSelected(isSelected);
        cartItemRepository.save(cartItem);

        logger.info("Selection updated successfully");
        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto selectAll(String userEmail, Boolean isSelected) {
        logger.info("Setting all items selection to {} for user: {}", isSelected, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        cartItemRepository.updateSelectionForCart(cart.getId(), isSelected);

        logger.info("All items selection updated");
        return getCart(userEmail);
    }

    @Transactional
    public CartResponseDto selectByShop(String userEmail, Long shopId, Boolean isSelected) {
        logger.info("Setting shop ID: {} items selection to {} for user: {}", shopId, isSelected, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);

        cartItemRepository.updateSelectionForShop(cart.getId(), shopId, isSelected);

        logger.info("Shop items selection updated");
        return getCart(userEmail);
    }

    public CartResponseDto getCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(user);
        return mapToResponseDto(cart);
    }

    public CartSummaryDto getCartSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Cart> cartOpt = cartRepository.findByUser(user);

        if (cartOpt.isEmpty()) {
            return new CartSummaryDto(null, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cart.getItems();

        BigDecimal subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal selectedSubtotal = items.stream()
                .filter(CartItem::getIsSelected)
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavings = items.stream()
                .filter(item -> item.getProduct().getCompareAtPrice() != null)
                .map(item -> {
                    BigDecimal compareAtPrice = item.getProduct().getCompareAtPrice();
                    BigDecimal saving = compareAtPrice.subtract(item.getUnitPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return saving.compareTo(BigDecimal.ZERO) > 0 ? saving : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int selectedCount = (int) items.stream()
                .filter(CartItem::getIsSelected)
                .count();

        return new CartSummaryDto(
                cart.getId(),
                cart.getTotalItems(),
                selectedCount,
                subtotal,
                selectedSubtotal,
                totalSavings
        );
    }

    public CartValidationResultDto validateCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Cart> cartOpt = cartRepository.findByUser(user);

        if (cartOpt.isEmpty()) {
            return new CartValidationResultDto(true, List.of(), List.of(), Map.of());
        }

        Cart cart = cartOpt.get();
        List<String> errors = new ArrayList<>();
        List<CartItemValidationDto> itemValidations = new ArrayList<>();
        Map<Long, Integer> availableStock = new HashMap<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            availableStock.put(product.getId(), product.getAvailableQuantity());

            CartItemValidationDto validation = validateCartItem(item);
            itemValidations.add(validation);

            if (!validation.isAvailable()) {
                errors.add(validation.error());
            }
        }

        boolean isValid = errors.isEmpty() && !cart.isExpired();

        if (cart.isExpired()) {
            errors.add("Cart has expired");
        }

        return new CartValidationResultDto(isValid, errors, itemValidations, availableStock);
    }

    @Transactional
    public CartResponseDto mergeGuestCart(String userEmail, String sessionId) {
        logger.info("Merging guest cart for session: {} with user: {}", sessionId, userEmail);

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) {
            return getCart(userEmail);
        }

        Cart guestCart = guestCartOpt.get();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart userCart = getOrCreateCart(user);

        // Merge items from guest cart to user cart
        for (CartItem guestItem : guestCart.getItems()) {
            try {
                AddToCartRequest request = new AddToCartRequest();
                request.setProductId(guestItem.getProduct().getId());
                request.setQuantity(guestItem.getQuantity());
                request.setNotes(guestItem.getNotes());
                request.setIsSelected(guestItem.getIsSelected());

                addToCart(userEmail, request);
            } catch (Exception e) {
                logger.warn("Failed to merge guest cart item: {}", e.getMessage());
            }
        }

        // Delete guest cart
        cartRepository.delete(guestCart);

        logger.info("Guest cart merged successfully");
        return getCart(userEmail);
    }

    public CartResponseDto getCartBySession(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createGuestCart(sessionId));

        if (cart.isExpired()) {
            cart = refreshGuestCart(cart);
        }

        return mapToResponseDto(cart);
    }

    @Transactional
    public CartResponseDto addToGuestCart(String sessionId, AddToCartRequest request) {
        logger.info("Adding product ID: {} to guest cart for session: {}", request.getProductId(), sessionId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        validateProductAvailability(product);

        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createGuestCart(sessionId));

        if (cart.isExpired()) {
            cart = refreshGuestCart(cart);
        }

        // Add item logic similar to user cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setUnitPrice(product.getPrice());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .shop(product.getShop())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .notes(request.getNotes())
                    .isSelected(request.getIsSelected())
                    .build();
        }

        cartItem.calculateTotalPrice();
        cartItemRepository.save(cartItem);

        cart.updateTotalItems();
        cartRepository.save(cart);

        return mapToResponseDto(cart);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupExpiredCarts() {
        logger.info("Cleaning up expired carts");
        int deletedCount = cartRepository.deleteExpiredCarts(LocalDateTime.now());
        logger.info("Deleted {} expired carts", deletedCount);
    }

    @Transactional
    public void refreshCartExpiry(Long cartId) {
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(cartExpiryDays);
        cartRepository.updateExpiryDate(cartId, newExpiry);
        logger.info("Refreshed expiry for cart ID: {} to {}", cartId, newExpiry);
    }

    // Private helper methods
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .expiresAt(LocalDateTime.now().plusDays(cartExpiryDays))
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
    }

    private Cart createGuestCart(String sessionId) {
        Cart cart = Cart.builder()
                .sessionId(sessionId)
                .expiresAt(LocalDateTime.now().plusDays(cartExpiryDays))
                .build();
        return cartRepository.save(cart);
    }

    private Cart refreshGuestCart(Cart cart) {
        cart.setExpiresAt(LocalDateTime.now().plusDays(cartExpiryDays));
        return cartRepository.save(cart);
    }

    private void validateProductAvailability(Product product) {
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new InvalidQuantityException("Product is not available for purchase");
        }
        if (!product.isInStock()) {
            throw new InvalidQuantityException("Product is out of stock");
        }
    }

    private CartItemValidationDto validateCartItem(CartItem item) {
        Product product = item.getProduct();
        boolean isAvailable = product.isInStock() &&
                product.getStatus() == ProductStatus.PUBLISHED &&
                item.getQuantity() <= product.getAvailableQuantity();

        String error = null;
        if (!isAvailable) {
            if (!product.isInStock()) {
                error = "Product is out of stock";
            } else if (product.getStatus() != ProductStatus.PUBLISHED) {
                error = "Product is no longer available";
            } else if (item.getQuantity() > product.getAvailableQuantity()) {
                error = String.format("Only %d available", product.getAvailableQuantity());
            }
        }

        return new CartItemValidationDto(
                item.getId(),
                product.getId(),
                product.getName(),
                item.getQuantity(),
                product.getAvailableQuantity(),
                isAvailable,
                error
        );
    }

    private CartResponseDto mapToResponseDto(Cart cart) {
        List<CartItemResponseDto> itemDtos = cart.getItems().stream()
                .map(this::mapToItemResponseDto)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemResponseDto::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal selectedSubtotal = itemDtos.stream()
                .filter(CartItemResponseDto::isSelected)
                .map(CartItemResponseDto::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavings = itemDtos.stream()
                .map(CartItemResponseDto::savings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal estimatedTax = selectedSubtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedShipping = calculateEstimatedShipping(cart);
        BigDecimal estimatedTotal = selectedSubtotal.add(estimatedTax).add(estimatedShipping);

        int selectedItemsCount = (int) itemDtos.stream()
                .filter(CartItemResponseDto::isSelected)
                .count();

        return new CartResponseDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getSessionId(),
                itemDtos,
                cart.getTotalItems(),
                selectedItemsCount,
                subtotal,
                selectedSubtotal,
                totalSavings,
                estimatedTax,
                estimatedShipping,
                estimatedTotal,
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cart.getExpiresAt(),
                cart.isExpired()
        );
    }

    private CartItemResponseDto mapToItemResponseDto(CartItem item) {
        Product product = item.getProduct();

        BigDecimal savings = BigDecimal.ZERO;
        if (product.getCompareAtPrice() != null &&
                product.getCompareAtPrice().compareTo(item.getUnitPrice()) > 0) {
            savings = product.getCompareAtPrice().subtract(item.getUnitPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
        }

        return new CartItemResponseDto(
                item.getId(),
                item.getCart().getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getMainImageUrl(),
                item.getShop().getId(),
                item.getShop().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                product.getCompareAtPrice(),
                item.getTotalPrice(),
                savings,
                product.isInStock(),
                product.getAvailableQuantity(),
                item.getNotes(),
                item.getIsSelected(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private BigDecimal calculateEstimatedShipping(Cart cart) {
        // TODO: Implement actual shipping calculation based on items, location, etc.
        return BigDecimal.ZERO;
    }
}