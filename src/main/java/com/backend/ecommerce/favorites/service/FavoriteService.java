// src/main/java/com/backend/ecommerce/favorites/service/FavoriteService.java
package com.backend.ecommerce.favorites.service;

import com.backend.ecommerce.favorites.exception.FavoriteAlreadyExistsException;
import com.backend.ecommerce.favorites.exception.FavoriteNotFoundException;
import com.backend.ecommerce.favorites.model.Favorite;
import com.backend.ecommerce.favorites.payload.*;
import com.backend.ecommerce.favorites.repository.FavoriteRepository;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.repository.ProductRepository;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository;
import com.backend.ecommerce.user.service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           EmailService emailService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    @Transactional
    public FavoriteResponseDto addToFavorites(String userEmail, FavoriteCreateRequest request)
            throws FavoriteAlreadyExistsException, ProductNotFoundException {

        logger.info("Adding product ID: {} to favorites for user: {}", request.getProductId(), userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + request.getProductId()));

        // Check if already in favorites
        if (favoriteRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new FavoriteAlreadyExistsException("Product already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .notes(request.getNotes())
                .notifyOnSale(request.getNotifyOnSale())
                .notifyOnStock(request.getNotifyOnStock())
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        logger.info("Product added to favorites successfully. Favorite ID: {}", savedFavorite.getId());

        return mapToResponseDto(savedFavorite);
    }

    @Transactional
    public List<FavoriteResponseDto> bulkAddToFavorites(String userEmail, BulkFavoriteRequest request) {
        logger.info("Bulk adding {} products to favorites for user: {}", request.getProductIds().size(), userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> existingFavoriteIds = favoriteRepository.findProductIdsByUserId(user.getId());

        List<Favorite> newFavorites = request.getProductIds().stream()
                .filter(productId -> !existingFavoriteIds.contains(productId))
                .map(productId -> {
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        return Favorite.builder()
                                .user(user)
                                .product(product)
                                .notifyOnSale(false)
                                .notifyOnStock(false)
                                .build();
                    }
                    return null;
                })
                .filter(favorite -> favorite != null)
                .collect(Collectors.toList());

        List<Favorite> savedFavorites = favoriteRepository.saveAll(newFavorites);
        logger.info("Added {} products to favorites", savedFavorites.size());

        return savedFavorites.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromFavorites(String userEmail, Long productId) throws FavoriteNotFoundException {
        logger.info("Removing product ID: {} from favorites for user: {}", productId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new FavoriteNotFoundException("Product not found in favorites");
        }

        favoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
        logger.info("Product removed from favorites successfully");
    }

    @Transactional
    public void bulkRemoveFromFavorites(String userEmail, BulkFavoriteRequest request) {
        logger.info("Bulk removing {} products from favorites for user: {}", request.getProductIds().size(), userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        favoriteRepository.deleteByUserIdAndProductIds(user.getId(), request.getProductIds());
        logger.info("Removed {} products from favorites", request.getProductIds().size());
    }

    @Transactional
    public FavoriteResponseDto updateFavorite(String userEmail, Long productId, FavoriteUpdateRequest request)
            throws FavoriteNotFoundException {

        logger.info("Updating favorite for product ID: {} for user: {}", productId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Favorite favorite = favoriteRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new FavoriteNotFoundException("Product not found in favorites"));

        if (request.getNotes() != null) {
            favorite.setNotes(request.getNotes());
        }
        if (request.getNotifyOnSale() != null) {
            favorite.setNotifyOnSale(request.getNotifyOnSale());
        }
        if (request.getNotifyOnStock() != null) {
            favorite.setNotifyOnStock(request.getNotifyOnStock());
        }

        Favorite updatedFavorite = favoriteRepository.save(favorite);
        logger.info("Favorite updated successfully");

        return mapToResponseDto(updatedFavorite);
    }

    public FavoriteListResponseDto getUserFavorites(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Favorite> favoritesPage = favoriteRepository.findByUserId(user.getId(), pageable);

        List<FavoriteResponseDto> favorites = favoritesPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return new FavoriteListResponseDto(
                favorites,
                favoritesPage.getTotalElements(),
                favoritesPage.getTotalPages(),
                favoritesPage.getNumber(),
                favoritesPage.getSize()
        );
    }

    public List<FavoriteResponseDto> getAllUserFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUserIdWithProductDetails(user.getId());

        return favorites.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public FavoriteListResponseDto searchUserFavorites(String userEmail, String keyword, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Favorite> favoritesPage = favoriteRepository.searchUserFavorites(user.getId(), keyword, pageable);

        List<FavoriteResponseDto> favorites = favoritesPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return new FavoriteListResponseDto(
                favorites,
                favoritesPage.getTotalElements(),
                favoritesPage.getTotalPages(),
                favoritesPage.getNumber(),
                favoritesPage.getSize()
        );
    }

    public FavoriteStatusResponseDto checkFavoriteStatus(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        java.util.Optional<Favorite> favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId);

        return new FavoriteStatusResponseDto(
                productId,
                favorite.isPresent(),
                favorite.map(Favorite::getId).orElse(null)
        );
    }

    public Map<Long, Boolean> checkBulkFavoriteStatus(String userEmail, List<Long> productIds) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> favoriteProductIds = favoriteRepository.findProductIdsByUserId(user.getId());

        return productIds.stream()
                .collect(Collectors.toMap(
                        productId -> productId,
                        favoriteProductIds::contains
                ));
    }

    public Long getFavoriteCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.countByUserId(user.getId());
    }

    @Transactional
    public void clearAllFavorites(String userEmail) {
        logger.info("Clearing all favorites for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        favoriteRepository.deleteAll(favorites);

        logger.info("Cleared {} favorites for user: {}", favorites.size(), userEmail);
    }

    @Transactional
    public void moveAllToCart(String userEmail) {
        logger.info("Moving all favorites to cart for user: {}", userEmail);
        // This will be implemented when cart service is available
        // For now, just get the favorites and later pass to cart service
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());
        // TODO: Call cart service to add all items
        logger.info("Found {} favorites to move to cart", favorites.size());
    }

    // Scheduled task to send notifications
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    @Transactional
    public void sendSaleNotifications() {
        logger.info("Checking for sale notifications");

        List<Favorite> saleFavorites = favoriteRepository.findFavoritesWithSaleNotifications();

        Map<User, List<Favorite>> userFavoritesMap = saleFavorites.stream()
                .collect(Collectors.groupingBy(Favorite::getUser));

        userFavoritesMap.forEach((user, favorites) -> {
            try {
                emailService.sendFavoriteSaleNotificationEmail(user, favorites);
                logger.info("Sent sale notification to user: {}", user.getEmail());
            } catch (EmailFailureException e) {
                logger.error("Failed to send sale notification to user: {}", user.getEmail(), e);
            }
        });
    }

    @Scheduled(cron = "0 0 10 * * ?") // Daily at 10 AM
    @Transactional
    public void sendStockNotifications() {
        logger.info("Checking for stock notifications");

        List<Favorite> stockFavorites = favoriteRepository.findFavoritesWithStockNotifications();

        Map<User, List<Favorite>> userFavoritesMap = stockFavorites.stream()
                .collect(Collectors.groupingBy(Favorite::getUser));

        userFavoritesMap.forEach((user, favorites) -> {
            try {
                emailService.sendFavoriteStockNotificationEmail(user, favorites);
                logger.info("Sent stock notification to user: {}", user.getEmail());
            } catch (EmailFailureException e) {
                logger.error("Failed to send stock notification to user: {}", user.getEmail(), e);
            }
        });
    }

    // Admin methods
    public Page<FavoriteResponseDto> getAllFavorites(Pageable pageable) {
        return favoriteRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    public List<FavoriteResponseDto> getUserFavoritesByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFavoritesByProductId(Long productId) {
        favoriteRepository.deleteByProductId(productId);
        logger.info("Deleted all favorites for product ID: {}", productId);
    }

    private FavoriteResponseDto mapToResponseDto(Favorite favorite) {
        Product product = favorite.getProduct();

        BigDecimal discountPercentage = BigDecimal.ZERO;
        if (product.getCompareAtPrice() != null &&
                product.getCompareAtPrice().compareTo(BigDecimal.ZERO) > 0 &&
                product.getPrice().compareTo(product.getCompareAtPrice()) < 0) {
            discountPercentage = product.getCompareAtPrice().subtract(product.getPrice())
                    .divide(product.getCompareAtPrice(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new FavoriteResponseDto(
                favorite.getId(),
                favorite.getUser().getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getCompareAtPrice(),
                discountPercentage,
                product.getMainImageUrl(),
                product.getQuantity(),
                product.isInStock(),
                product.getCompareAtPrice() != null &&
                        product.getPrice().compareTo(product.getCompareAtPrice()) < 0,
                product.getShop().getName(),
                product.getShop().getSlug(),
                favorite.getNotes(),
                favorite.getNotifyOnSale(),
                favorite.getNotifyOnStock(),
                favorite.getCreatedAt()
        );
    }
}