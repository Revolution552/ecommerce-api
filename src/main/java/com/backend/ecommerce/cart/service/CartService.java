package com.backend.ecommerce.cart.service;

import com.backend.ecommerce.cart.dao.CartDAO;
import com.backend.ecommerce.cart.dao.CartItemDAO;
import com.backend.ecommerce.cart.model.Cart;
import com.backend.ecommerce.cart.model.CartItem;
import com.backend.ecommerce.cart.payload.CartDTO;
import com.backend.ecommerce.cart.payload.CartItemDTO;
import com.backend.ecommerce.products.product.payload.ProductResponseDTO;
import com.backend.ecommerce.products.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing shopping cart business logic.
 * Handles operations like adding/removing items, updating quantities,
 * retrieving cart contents, and creating/managing user carts.
 */
@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductService productService;

    @Autowired
    public CartService(CartDAO cartDAO, CartItemDAO cartItemDAO, ProductService productService) {
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productService = productService;
    }

    /**
     * Retrieves a user's cart. If the user does not have a cart, a new one is created.
     * @param userId The ID of the user.
     * @return The CartDTO for the user's cart.
     */
    @Transactional
    public CartDTO getOrCreateCart(Long userId) {
        logger.info("Fetching or creating cart for user ID: {}", userId);
        Optional<Cart> existingCart = cartDAO.findByUserId(userId);
        if (existingCart.isPresent()) {
            logger.info("Cart found for user ID: {}", userId);
            return convertToDTO(existingCart.get());
        } else {
            logger.warn("No cart found for user ID: {}. Creating a new one.", userId);
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            Cart savedCart = cartDAO.save(newCart);
            logger.info("New cart created successfully with ID: {} for user ID: {}", savedCart.getId(), userId);
            return convertToDTO(savedCart);
        }
    }

    /**
     * Adds a product to the cart or updates its quantity if it already exists.
     * @param userId The ID of the user.
     * @param cartItemDTO The CartItemDTO containing product ID and quantity.
     * @return The updated CartDTO.
     * @throws EntityNotFoundException if the product does not exist.
     * @throws IllegalArgumentException if quantity is invalid.
     */
    @Transactional
    public CartDTO addOrUpdateCartItem(Long userId, CartItemDTO cartItemDTO) {
        logger.info("Adding or updating item for user ID: {}, Product ID: {}, Quantity: {}", userId, cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        if (cartItemDTO.getQuantity() <= 0) {
            logger.error("Invalid quantity provided: {}", cartItemDTO.getQuantity());
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Cart cart = cartDAO.findByUserId(userId)
                .orElseGet(() -> {
                    logger.warn("No cart found for user ID: {}. Creating a new one to add item.", userId);
                    Cart newCart = Cart.builder().userId(userId).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                    return cartDAO.save(newCart);
                });

        ProductResponseDTO productResponseDTO = productService.getProductDetails(cartItemDTO.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product with ID: {} not found.", cartItemDTO.getProductId());
                    return new EntityNotFoundException("Product not found with ID: " + cartItemDTO.getProductId());
                });

        Optional<CartItem> existingCartItem = cartItemDAO.findByCartIdAndProductId(cart.getId(), cartItemDTO.getProductId());

        if (existingCartItem.isPresent()) {
            CartItem item = existingCartItem.get();
            logger.info("Product with ID: {} already exists in cart. Updating quantity from {} to {}.",
                    cartItemDTO.getProductId(), item.getQuantity(), item.getQuantity() + cartItemDTO.getQuantity());
            item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            item.setPrice(BigDecimal.valueOf(productResponseDTO.getPrice()));
            cartItemDAO.save(item);
        } else {
            logger.info("Adding new product with ID: {} to cart.", cartItemDTO.getProductId());
            CartItem newItem = CartItem.builder()
                    .productId(cartItemDTO.getProductId())
                    .quantity(cartItemDTO.getQuantity())
                    .price(BigDecimal.valueOf(productResponseDTO.getPrice()))
                    .build();
            cart.addCartItem(newItem);
        }

        Cart updatedCart = cartDAO.save(cart);
        logger.info("Cart for user ID: {} updated successfully.", userId);
        return convertToDTO(updatedCart);
    }

    /**
     * Updates the quantity of a specific item in the cart.
     * If new quantity is 0 or less, the item is removed.
     * @param userId The ID of the user.
     * @param productId The ID of the product to update.
     * @param newQuantity The new quantity for the item.
     * @return The updated CartDTO.
     * @throws EntityNotFoundException if the cart or cart item is not found.
     * @throws IllegalArgumentException if new quantity is negative.
     */
    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        logger.info("Updating quantity for user ID: {}, Product ID: {}, New Quantity: {}", userId, productId, newQuantity);
        if (newQuantity < 0) {
            logger.error("Invalid new quantity provided: {}", newQuantity);
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        Cart cart = cartDAO.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user ID: {} for quantity update.", userId);
                    return new EntityNotFoundException("Cart not found for user ID: " + userId);
                });

        CartItem itemToUpdate = cartItemDAO.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> {
                    logger.error("Product with ID: {} not found in cart for user ID: {} for quantity update.", productId, userId);
                    return new EntityNotFoundException("Product " + productId + " not found in cart for user ID: " + userId);
                });

        if (newQuantity == 0) {
            logger.info("Removing product with ID: {} from cart as new quantity is zero.", productId);
            cart.removeCartItem(itemToUpdate);
        } else {
            logger.info("Updating product with ID: {} quantity from {} to {}.", productId, itemToUpdate.getQuantity(), newQuantity);
            itemToUpdate.setQuantity(newQuantity);
            cartItemDAO.save(itemToUpdate);
        }

        Cart updatedCart = cartDAO.save(cart);
        logger.info("Cart item quantity updated successfully for user ID: {}.", userId);
        return convertToDTO(updatedCart);
    }

    /**
     * Removes a specific item from the cart.
     * @param userId The ID of the user.
     * @param productId The ID of the product to remove.
     * @return The updated CartDTO.
     * @throws EntityNotFoundException if the cart or cart item is not found.
     */
    @Transactional
    public CartDTO removeCartItem(Long userId, Long productId) {
        logger.warn("Attempting to remove product with ID: {} from cart for user ID: {}", productId, userId);
        Cart cart = cartDAO.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user ID: {} for item removal.", userId);
                    return new EntityNotFoundException("Cart not found for user ID: " + userId);
                });

        CartItem itemToRemove = cartItemDAO.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> {
                    logger.error("Product with ID: {} not found in cart for user ID: {} for item removal.", productId, userId);
                    return new EntityNotFoundException("Product " + productId + " not found in cart for user ID: " + userId);
                });

        cart.removeCartItem(itemToRemove);
        Cart updatedCart = cartDAO.save(cart);
        logger.info("Product with ID: {} removed successfully from cart for user ID: {}.", productId, userId);
        return convertToDTO(updatedCart);
    }

    /**
     * Clears all items from a user's cart.
     * @param userId The ID of the user.
     * @return The updated CartDTO (empty cart).
     * @throws EntityNotFoundException if the cart is not found.
     */
    @Transactional
    public CartDTO clearCart(Long userId) {
        logger.warn("Attempting to clear cart for user ID: {}", userId);
        Cart cart = cartDAO.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user ID: {} for clear operation.", userId);
                    return new EntityNotFoundException("Cart not found for user ID: " + userId);
                });

        cart.getCartItems().clear();
        Cart clearedCart = cartDAO.save(cart);
        logger.info("Cart cleared successfully for user ID: {}.", userId);
        return convertToDTO(clearedCart);
    }

    /**
     * Converts a Cart entity to a CartDTO.
     * @param cart The Cart entity to convert.
     * @return The corresponding CartDTO.
     */
    private CartDTO convertToDTO(Cart cart) {
        logger.debug("Converting Cart entity with ID: {} to DTO.", cart.getId());
        List<CartItemDTO> itemDTOs = cart.getCartItems().stream()
                .map(item -> CartItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.debug("Calculated total amount for cart {}: {}", cart.getId(), totalAmount);
        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .cartItems(itemDTOs)
                .totalAmount(totalAmount)
                .build();
    }
}