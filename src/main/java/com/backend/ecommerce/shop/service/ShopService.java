// src/main/java/com/backend/ecommerce/shop/service/ShopService.java
package com.backend.ecommerce.shop.service;

import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.exception.SellerNotVerifiedException;
import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.seller.model.SellerVerificationStatus;
import com.backend.ecommerce.seller.repository.SellerRepository;
import com.backend.ecommerce.shop.exception.*;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.shop.model.ShopStatus;
import com.backend.ecommerce.shop.payload.ShopApprovalRequest;
import com.backend.ecommerce.shop.payload.ShopCreateRequest;
import com.backend.ecommerce.shop.payload.ShopResponseDto;
import com.backend.ecommerce.shop.payload.ShopUpdateRequest;
import com.backend.ecommerce.shop.repository.ShopRepository;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);

    private final ShopRepository shopRepository;
    private final SellerRepository sellerRepository;
    private final EmailService emailService;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public ShopService(ShopRepository shopRepository,
                       SellerRepository sellerRepository,
                       EmailService emailService) {
        this.shopRepository = shopRepository;
        this.sellerRepository = sellerRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ShopResponseDto createShop(ShopCreateRequest request, Long adminUserId)
            throws ShopAlreadyExistsException, InvalidSellerException, EmailFailureException {

        logger.info("Creating shop: {} for seller ID: {}", request.getName(), request.getSellerId());

        // Check if shop name already exists
        if (shopRepository.existsByName(request.getName())) {
            throw new ShopAlreadyExistsException("Shop with name '" + request.getName() + "' already exists");
        }

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check if slug already exists
        if (shopRepository.existsBySlug(slug)) {
            throw new ShopAlreadyExistsException("Shop with slug '" + slug + "' already exists");
        }

        // Find and validate seller
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new InvalidSellerException("Seller not found with ID: " + request.getSellerId()));

        // Check if seller is verified
        if (seller.getVerificationStatus() != SellerVerificationStatus.APPROVED) {
            throw new InvalidSellerException("Seller is not verified. Current status: " +
                    seller.getVerificationStatus());
        }

        // Check if seller already has a shop
        if (shopRepository.existsBySellerId(seller.getId())) {
            throw new InvalidSellerException("Seller already has a shop assigned");
        }

        // Create shop
        Shop shop = Shop.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .seller(seller)
                .status(ShopStatus.PENDING)
                .isActive(true)
                .isFeatured(false)
                .rating(0.0)
                .totalReviews(0)
                .totalProducts(0)
                .totalSales(0)
                .build();

        Shop savedShop = shopRepository.save(shop);

        // Send email notification to seller
        try {
            emailService.sendShopCreatedEmail(savedShop, seller);
        } catch (EmailFailureException e) {
            logger.error("Failed to send shop creation email", e);
            // Don't rollback shop creation
        }

        logger.info("Shop created successfully: {} with slug: {}", savedShop.getName(), savedShop.getSlug());
        return mapToResponseDto(savedShop);
    }

    @Transactional
    public ShopResponseDto approveShop(Long shopId, ShopApprovalRequest request, Long adminUserId)
            throws ShopNotFoundException, EmailFailureException {

        logger.info("Processing shop approval for shop ID: {} by admin: {}", shopId, adminUserId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with ID: " + shopId));

        ShopStatus newStatus = request.getApproved() ? ShopStatus.APPROVED : ShopStatus.REJECTED;
        ShopStatus oldStatus = shop.getStatus();

        shop.setStatus(newStatus);
        shop.setRejectionReason(request.getRejectionReason());

        if (request.getApproved()) {
            shop.setApprovedAt(LocalDateTime.now());
            shop.setApprovedBy(adminUserId);
        }

        Shop updatedShop = shopRepository.save(shop);
        Seller seller = shop.getSeller();

        // Send appropriate email
        try {
            if (request.getApproved()) {
                emailService.sendShopApprovedEmail(updatedShop, seller);
            } else {
                emailService.sendShopRejectedEmail(updatedShop, seller, request.getRejectionReason());
            }
        } catch (EmailFailureException e) {
            logger.error("Failed to send shop approval status email", e);
        }

        logger.info("Shop status updated from {} to {} for shop: {}", oldStatus, newStatus, shop.getName());
        return mapToResponseDto(updatedShop);
    }

    @Transactional
    public ShopResponseDto updateShop(Long shopId, ShopUpdateRequest request) throws ShopNotFoundException {
        logger.info("Updating shop with ID: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with ID: " + shopId));

        if (request.getName() != null && !request.getName().equals(shop.getName())) {
            if (shopRepository.existsByName(request.getName())) {
                throw new ShopAlreadyExistsException("Shop with name '" + request.getName() + "' already exists");
            }
            shop.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equals(shop.getSlug())) {
            if (shopRepository.existsBySlug(request.getSlug())) {
                throw new ShopAlreadyExistsException("Shop with slug '" + request.getSlug() + "' already exists");
            }
            shop.setSlug(request.getSlug());
        }

        if (request.getDescription() != null) {
            shop.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            shop.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            shop.setBannerUrl(request.getBannerUrl());
        }
        if (request.getAddress() != null) {
            shop.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            shop.setCity(request.getCity());
        }
        if (request.getState() != null) {
            shop.setState(request.getState());
        }
        if (request.getCountry() != null) {
            shop.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            shop.setPostalCode(request.getPostalCode());
        }
        if (request.getPhone() != null) {
            shop.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            shop.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            shop.setWebsite(request.getWebsite());
        }
        if (request.getIsActive() != null) {
            shop.setIsActive(request.getIsActive());
        }
        if (request.getIsFeatured() != null) {
            shop.setIsFeatured(request.getIsFeatured());
        }

        Shop updatedShop = shopRepository.save(shop);
        logger.info("Shop updated successfully: {}", updatedShop.getName());
        return mapToResponseDto(updatedShop);
    }

    @Transactional
    public void deleteShop(Long shopId) throws ShopNotFoundException {
        logger.info("Deleting shop with ID: {}", shopId);

        if (!shopRepository.existsById(shopId)) {
            throw new ShopNotFoundException("Shop not found with ID: " + shopId);
        }

        shopRepository.deleteById(shopId);
        logger.info("Shop deleted successfully with ID: {}", shopId);
    }

    public ShopResponseDto getShopById(Long shopId) throws ShopNotFoundException {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with ID: " + shopId));
        return mapToResponseDto(shop);
    }

    public ShopResponseDto getShopBySlug(String slug) throws ShopNotFoundException {
        Shop shop = shopRepository.findBySlug(slug)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with slug: " + slug));
        return mapToResponseDto(shop);
    }

    public ShopResponseDto getShopBySellerId(Long sellerId) throws ShopNotFoundException {
        Shop shop = shopRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found for seller ID: " + sellerId));
        return mapToResponseDto(shop);
    }

    public List<ShopResponseDto> getAllShops() {
        return shopRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ShopResponseDto> getAllShopsPaginated(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    public List<ShopResponseDto> getShopsByStatus(ShopStatus status) {
        return shopRepository.findByStatus(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ShopResponseDto> getShopsByStatusPaginated(ShopStatus status, Pageable pageable) {
        return shopRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDto);
    }

    public List<ShopResponseDto> getActiveShops() {
        return shopRepository.findByIsActiveTrue().stream()
                .filter(shop -> shop.getStatus() == ShopStatus.APPROVED)
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ShopResponseDto> getFeaturedShops() {
        return shopRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .filter(shop -> shop.getStatus() == ShopStatus.APPROVED)
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ShopResponseDto> searchShops(String keyword, Pageable pageable) {
        return shopRepository.searchShops(keyword, pageable)
                .map(this::mapToResponseDto);
    }

    public Shop getShopEntityById(Long shopId) throws ShopNotFoundException {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with ID: " + shopId));
    }

    public void validateShopIsActive(Shop shop) throws ShopNotApprovedException {
        if (!shop.getIsActive()) {
            throw new ShopNotApprovedException("Shop is not active");
        }
        if (shop.getStatus() != ShopStatus.APPROVED) {
            throw new ShopNotApprovedException("Shop is not approved. Current status: " + shop.getStatus());
        }
    }

    @Transactional
    public void incrementProductCount(Long shopId) {
        shopRepository.findById(shopId).ifPresent(shop -> {
            shop.setTotalProducts(shop.getTotalProducts() + 1);
            shopRepository.save(shop);
        });
    }

    @Transactional
    public void decrementProductCount(Long shopId) {
        shopRepository.findById(shopId).ifPresent(shop -> {
            shop.setTotalProducts(Math.max(0, shop.getTotalProducts() - 1));
            shopRepository.save(shop);
        });
    }

    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private ShopResponseDto mapToResponseDto(Shop shop) {
        Seller seller = shop.getSeller();
        User user = seller.getUser();

        return new ShopResponseDto(
                shop.getId(),
                shop.getName(),
                shop.getSlug(),
                shop.getDescription(),
                shop.getLogoUrl(),
                shop.getBannerUrl(),
                shop.getAddress(),
                shop.getCity(),
                shop.getState(),
                shop.getCountry(),
                shop.getPostalCode(),
                shop.getPhone(),
                shop.getEmail(),
                shop.getWebsite(),
                seller.getId(),
                seller.getBusinessName(),
                user.getEmail(),
                shop.getStatus(),
                shop.getRejectionReason(),
                shop.getCreatedAt(),
                shop.getUpdatedAt(),
                shop.getApprovedAt(),
                shop.getIsActive(),
                shop.getIsFeatured(),
                shop.getRating(),
                shop.getTotalReviews(),
                shop.getTotalProducts(),
                shop.getTotalSales()
        );
    }
}