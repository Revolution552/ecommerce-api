package com.backend.ecommerce.products.shop.service;

import com.backend.ecommerce.products.shop.dao.ShopDAO;
import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.products.shop.payload.ShopDTO;
import com.backend.ecommerce.products.product.service.ImageService;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository; // Changed from dao to repository
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    private final ShopDAO shopDAO;
    private final ImageService imageService;
    private final UserRepository userRepository; // Changed from UserDAO to UserRepository

    public ShopService(ShopDAO shopDAO, ImageService imageService, UserRepository userRepository) { // Updated constructor parameter
        this.shopDAO = shopDAO;
        this.imageService = imageService;
        this.userRepository = userRepository; // Initialized UserRepository
    }

    /**
     * Retrieves all shops and converts them to DTOs.
     * @return a list of all shops as DTOs.
     */
    public List<ShopDTO> getAllShops() {
        logger.info("Fetching all shops");
        List<Shop> shops = shopDAO.findAll();
        logger.info("Found {} shops", shops.size());
        return shops.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves all shops with pagination support and converts them to DTOs.
     * @param pageable pagination details.
     * @return a paginated list of all shops as DTOs.
     */
    public Page<ShopDTO> getAllShopsPaginated(Pageable pageable) {
        logger.info("Fetching all shops with pagination: Page {}, Size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Shop> shopPage = shopDAO.findAll(pageable);
        logger.info("Found {} shops on page {}", shopPage.getNumberOfElements(), pageable.getPageNumber());
        return shopPage.map(this::convertToDTO);
    }

    /**
     * Creates a new shop in the database, associating it with the provided user ID.
     * @param shopDTO the shop data transfer object (without userId).
     * @param logoFile the logo image file for the shop.
     * @param ownerId the ID of the user who is creating the shop, derived from JWT.
     * @return the created shop as a DTO.
     * @throws IllegalArgumentException if the owner user is not found.
     */
    @Transactional
    public ShopDTO createShop(ShopDTO shopDTO, MultipartFile logoFile, Long ownerId) {
        logger.info("Creating new shop: {} for owner ID: {}", shopDTO.getName(), ownerId);

        // Fetch the actual User entity using the ownerId using userRepository
        User owner = userRepository.findById(ownerId) // Changed from userDAO.findById to userRepository.findById
                .orElseThrow(() -> {
                    logger.error("User with ID {} not found for shop creation.", ownerId);
                    return new IllegalArgumentException("Shop owner not found");
                });

        Shop shop = new Shop(); // Create a new Shop entity
        shop.setName(shopDTO.getName());
        shop.setLocation(shopDTO.getLocation());
        shop.setDescription(shopDTO.getDescription());
        shop.setUser(owner); // Set the retrieved User entity as the owner

        // Handle logo upload if a file is present
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoUrl = imageService.uploadImage(logoFile);
            shop.setLogoUrl(logoUrl);
        }

        Shop savedShop = shopDAO.save(shop);
        logger.info("Shop created successfully with ID: {} and owner ID: {}", savedShop.getId(), savedShop.getUser().getId());
        return convertToDTO(savedShop);
    }

    /**
     * Fetches a shop by its ID.
     * @param id the ID of the shop.
     * @return an Optional containing the Shop entity if found, otherwise empty.
     */
    public Optional<Shop> getShopById(Long id) {
        logger.info("Fetching shop with ID: {}", id);
        return shopDAO.findById(id);
    }

    /**
     * Fetches shops owned by a specific user.
     * @param userId the ID of the user.
     * @return a list of shops owned by the user as DTOs.
     */
    public List<ShopDTO> getShopsByUserId(Long userId) {
        logger.info("Fetching shops for user ID: {}", userId);
        List<Shop> shops = shopDAO.findByUserId(userId);
        logger.info("Found {} shops for user ID: {}", shops.size(), userId);
        return shops.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Finds shops by exact name, ignoring case sensitivity.
     * @param name the name of the shop.
     * @return a list of shops with an exact name match, case-insensitive, as DTOs.
     */
    public List<ShopDTO> getShopsByName(String name) {
        logger.info("Searching shops by exact name (case-insensitive): {}", name);
        List<Shop> shops = shopDAO.findByNameIgnoreCase(name);
        logger.info("Found {} shops with name matching '{}'", shops.size(), name);
        return shops.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Finds shops by location, ignoring case sensitivity.
     * @param location the location of the shop.
     * @return a list of shops located in the specified location, case-insensitive, as DTOs.
     */
    public List<ShopDTO> getShopsByLocation(String location) {
        logger.info("Searching shops by exact location (case-insensitive): {}", location);
        List<Shop> shops = shopDAO.findByLocationIgnoreCase(location);
        logger.info("Found {} shops in location matching '{}'", shops.size(), location);
        return shops.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Updates an existing shop in the database.
     * @param id the ID of the shop to update.
     * @param shopDetails the updated shop data transfer object.
     * @param updaterId the ID of the user performing the update (for authorization checks if needed).
     * @return the updated shop as a DTO.
     * @throws RuntimeException if the shop is not found.
     */
    @Transactional
    public ShopDTO updateShop(Long id, ShopDTO shopDetails, Long updaterId) {
        logger.info("Updating shop with ID: {} by user ID: {}", id, updaterId);

        Shop existingShop = shopDAO.findById(id)
                .orElseThrow(() -> {
                    logger.error("Shop not found with ID: {}", id);
                    return new RuntimeException("Shop not found");
                });

        // Optional: Add an authorization check here to ensure the updaterId matches the shop's owner ID
        if (!existingShop.getUser().getId().equals(updaterId)) {
            logger.warn("Unauthorized attempt to update shop ID {} by user ID {}", id, updaterId);
            throw new RuntimeException("Unauthorized: You do not own this shop.");
        }

        existingShop.setName(shopDetails.getName());
        existingShop.setLocation(shopDetails.getLocation());
        existingShop.setDescription(shopDetails.getDescription());

        // Do NOT update userId from shopDetails, as it's now handled by JWT.
        // If shopDetails has a userId, it should be ignored or used only for display on frontend.
        // The owner is determined by the authenticated user's token.

        Shop updatedShop = shopDAO.save(existingShop);
        logger.info("Shop updated successfully with ID: {}", updatedShop.getId());
        return convertToDTO(updatedShop);
    }

    /**
     * Deletes a shop from the database by ID, including its associated logo.
     * @param id the ID of the shop to delete.
     * @param deleterId the ID of the user performing the deletion (for authorization checks if needed).
     * @throws RuntimeException if the shop is not found or unauthorized access.
     */
    @Transactional
    public void deleteShop(Long id, Long deleterId) {
        logger.warn("Attempting to delete shop with ID: {} by user ID: {}", id, deleterId);
        Shop shop = shopDAO.findById(id)
                .orElseThrow(() -> {
                    logger.error("Attempted to delete non-existent shop with ID: {}", id);
                    return new RuntimeException("Shop not found");
                });

        // Optional: Add an authorization check here to ensure the deleterId matches the shop's owner ID
        if (!shop.getUser().getId().equals(deleterId)) {
            logger.warn("Unauthorized attempt to delete shop ID {} by user ID {}", id, deleterId);
            throw new RuntimeException("Unauthorized: You do not own this shop.");
        }

        // Delete the associated logo from S3
        String logoUrl = shop.getLogoUrl();
        if (logoUrl != null && !logoUrl.isEmpty()) {
            imageService.deleteImage(logoUrl);
        }

        shopDAO.delete(shop);
        logger.info("Shop with ID: {} deleted successfully", id);
    }

    /**
     * Converts a Shop entity to a ShopDTO.
     * This method is public to allow usage in controllers or other services.
     * @param shop The Shop entity to convert.
     * @return The corresponding ShopDTO.
     */
    public ShopDTO convertToDTO(Shop shop) {
        return new ShopDTO(
                shop.getId(),
                shop.getName(),
                shop.getLocation(),
                shop.getDescription(),
                shop.getUser() != null ? shop.getUser().getId() : null, // userId still part of DTO for reading
                shop.getLogoUrl()
        );
    }

    /**
     * Converts a ShopDTO to a Shop entity.
     * This method is private as it's typically an internal helper for the service.
     * IMPORTANT: This method should NOT be used for creating a new shop directly with a userId from DTO.
     * It's more for internal conversions where a user entity is already established.
     * @param shopDTO The ShopDTO to convert.
     * @return The corresponding Shop entity.
     */
    private Shop convertToEntity(ShopDTO shopDTO) {
        Shop shop = new Shop();
        shop.setId(shopDTO.getId());
        shop.setName(shopDTO.getName());
        shop.setLocation(shopDTO.getLocation());
        shop.setDescription(shopDTO.getDescription());
        shop.setLogoUrl(shopDTO.getLogoUrl());

        // The userId from shopDTO should generally be ignored for new creations,
        // as it's meant to be derived from JWT in the controller.
        // For existing shops where userId might be part of the DTO for updates,
        // it's crucial that the controller ensures authorization.
        if (shopDTO.getUserId() != null) {
            // This part is retained for cases where a userId might legitimately be set on an existing shop DTO
            // for internal logic or data display, but for shop creation, the controller will override it.
            User user = new User();
            user.setId(shopDTO.getUserId());
            shop.setUser(user);
        }

        return shop;
    }
}