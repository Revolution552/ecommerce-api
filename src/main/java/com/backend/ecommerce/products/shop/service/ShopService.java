package com.backend.ecommerce.products.shop.service;

import com.backend.ecommerce.products.shop.dao.ShopDAO;
import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.products.shop.payload.ShopDTO;
import com.backend.ecommerce.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    private final ShopDAO shopDAO;

    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }

    public List<ShopDTO> getAllShops() {
        logger.info("Fetching all shops");
        List<Shop> shops = shopDAO.findAll();
        logger.info("Found {} shops", shops.size());
        return shops.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ShopDTO createShop(ShopDTO shopDTO) {
        logger.info("Creating new shop: {}", shopDTO.getName());

        Shop shop = convertToEntity(shopDTO);

        // Validate that the owner is provided
        if (shop.getUser() == null) {
            logger.error("Failed to create shop: owner is null for shop '{}'", shopDTO.getName());
            throw new IllegalArgumentException("Shop owner cannot be null");
        }

        Shop savedShop = shopDAO.save(shop);
        logger.info("Shop created successfully with ID: {} and owner ID: {}", savedShop.getId(), savedShop.getUser().getId());
        return convertToDTO(savedShop);
    }

    // Method to fetch a shop by its ID
    public Optional<Shop> getShopById(Long id) {
        logger.info("Fetching shop with ID: {}", id);
        return shopDAO.findById(id);
    }

    public ShopDTO updateShop(Long id, ShopDTO shopDetails) {
        logger.info("Updating shop with ID: {}", id);

        Shop existingShop = shopDAO.findById(id)
                .orElseThrow(() -> {
                    logger.error("Shop not found with ID: {}", id);
                    return new RuntimeException("Shop not found");
                });

        // Update shop details
        existingShop.setName(shopDetails.getName());
        existingShop.setLocation(shopDetails.getLocation());
        existingShop.setDescription(shopDetails.getDescription());

        // Update the owner if provided
        if (shopDetails.getUserId() != null) {
            User user = new User();  // Assuming you can retrieve user by ID or pass user object
            user.setId(shopDetails.getUserId());
            existingShop.setUser(user);
        }

        Shop updatedShop = shopDAO.save(existingShop);
        logger.info("Shop updated successfully with ID: {}", updatedShop.getId());
        return convertToDTO(updatedShop);
    }

    public void deleteShop(Long id) {
        logger.warn("Attempting to delete shop with ID: {}", id);
        if (shopDAO.existsById(id)) {
            shopDAO.deleteById(id);
            logger.info("Shop with ID: {} deleted successfully", id);
        } else {
            logger.error("Attempted to delete non-existent shop with ID: {}", id);
            throw new RuntimeException("Shop not found");
        }
    }

    // Conversion methods
    private ShopDTO convertToDTO(Shop shop) {
        return new ShopDTO(shop.getId(), shop.getName(), shop.getLocation(), shop.getDescription(),
                shop.getUser() != null ? shop.getUser().getId() : null);
    }

    private Shop convertToEntity(ShopDTO shopDTO) {
        Shop shop = new Shop();
        shop.setId(shopDTO.getId());
        shop.setName(shopDTO.getName());
        shop.setLocation(shopDTO.getLocation());
        shop.setDescription(shopDTO.getDescription());

        if (shopDTO.getUserId() != null) {
            User user = new User(); // Retrieve or instantiate user based on your logic
            user.setId(shopDTO.getUserId());
            shop.setUser(user);
        }

        return shop;
    }
}
