// src/main/java/com/backend/ecommerce/seller/service/SellerService.java
package com.backend.ecommerce.seller.service;

import com.backend.ecommerce.seller.exception.SellerAlreadyExistsException;
import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.exception.SellerNotVerifiedException;
import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.seller.model.SellerVerificationStatus;
import com.backend.ecommerce.seller.payload.SellerRegistrationRequest;
import com.backend.ecommerce.seller.payload.SellerResponseDto;
import com.backend.ecommerce.seller.payload.SellerVerificationRequest;
import com.backend.ecommerce.seller.repository.SellerRepository;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository;
import com.backend.ecommerce.user.service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public SellerService(SellerRepository sellerRepository,
                         UserRepository userRepository,
                         EmailService emailService) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public SellerResponseDto registerAsSeller(String userEmail, SellerRegistrationRequest request)
            throws SellerAlreadyExistsException, EmailFailureException {

        logger.info("Processing seller registration for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is already a seller
        if (sellerRepository.existsByUserId(user.getId())) {
            throw new SellerAlreadyExistsException("User is already registered as a seller");
        }

        // Check if business email is already used
        if (request.getBusinessEmail() != null && !request.getBusinessEmail().isEmpty()) {
            if (sellerRepository.existsByBusinessEmail(request.getBusinessEmail())) {
                throw new SellerAlreadyExistsException("Business email is already registered");
            }
        }

        // Check if tax ID is already used
        if (request.getTaxId() != null && !request.getTaxId().isEmpty()) {
            if (sellerRepository.existsByTaxId(request.getTaxId())) {
                throw new SellerAlreadyExistsException("Tax ID is already registered");
            }
        }

        // Create seller
        Seller seller = Seller.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessEmail(request.getBusinessEmail())
                .businessPhone(request.getBusinessPhone())
                .businessAddress(request.getBusinessAddress())
                .taxId(request.getTaxId())
                .idCardNumber(request.getIdCardNumber())
                .bankAccountName(request.getBankAccountName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankName(request.getBankName())
                .verificationStatus(SellerVerificationStatus.PENDING)
                .isActive(true)
                .build();

        Seller savedSeller = sellerRepository.save(seller);

        // Send confirmation email
        try {
            emailService.sendSellerRegistrationConfirmation(savedSeller);
        } catch (EmailFailureException e) {
            logger.error("Failed to send seller registration confirmation email", e);
            // Don't rollback the registration, just log the error
        }

        logger.info("Seller registration completed for user: {}", userEmail);
        return mapToResponseDto(savedSeller);
    }

    @Transactional
    public SellerResponseDto verifySeller(Long sellerId, SellerVerificationRequest request, Long adminUserId)
            throws SellerNotFoundException, EmailFailureException {

        logger.info("Processing seller verification for seller ID: {} by admin: {}", sellerId, adminUserId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException("Seller not found with ID: " + sellerId));

        SellerVerificationStatus oldStatus = seller.getVerificationStatus();
        seller.setVerificationStatus(request.getStatus());
        seller.setVerificationNotes(request.getNotes());
        seller.setVerifiedBy(adminUserId);

        if (request.getStatus() == SellerVerificationStatus.APPROVED) {
            seller.setVerifiedAt(LocalDateTime.now());
        }

        Seller updatedSeller = sellerRepository.save(seller);

        // Send appropriate email based on verification status
        try {
            if (request.getStatus() == SellerVerificationStatus.APPROVED &&
                    oldStatus != SellerVerificationStatus.APPROVED) {
                emailService.sendSellerVerificationApprovedEmail(updatedSeller);
            } else if (request.getStatus() == SellerVerificationStatus.REJECTED) {
                emailService.sendSellerVerificationRejectedEmail(updatedSeller, request.getNotes());
            }
        } catch (EmailFailureException e) {
            logger.error("Failed to send seller verification status email", e);
            // Don't rollback the verification, just log the error
        }

        logger.info("Seller verification completed. Status changed from {} to {}", oldStatus, request.getStatus());
        return mapToResponseDto(updatedSeller);
    }

    public SellerResponseDto getSellerProfile(String userEmail) throws SellerNotFoundException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new SellerNotFoundException("Seller profile not found"));

        return mapToResponseDto(seller);
    }

    public SellerResponseDto getSellerById(Long sellerId) throws SellerNotFoundException {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException("Seller not found with ID: " + sellerId));
        return mapToResponseDto(seller);
    }

    public List<SellerResponseDto> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<SellerResponseDto> getSellersByStatus(SellerVerificationStatus status) {
        return sellerRepository.findByVerificationStatus(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Seller getVerifiedSellerByUserId(Long userId) throws SellerNotFoundException, SellerNotVerifiedException {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new SellerNotFoundException("Seller not found for user ID: " + userId));

        if (seller.getVerificationStatus() != SellerVerificationStatus.APPROVED) {
            throw new SellerNotVerifiedException("Seller is not verified. Current status: " +
                    seller.getVerificationStatus());
        }

        if (!seller.getIsActive()) {
            throw new SellerNotVerifiedException("Seller account is not active");
        }

        return seller;
    }

    public Seller getVerifiedSellerByEmail(String userEmail) throws SellerNotFoundException, SellerNotVerifiedException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getVerifiedSellerByUserId(user.getId());
    }

    private SellerResponseDto mapToResponseDto(Seller seller) {
        User user = seller.getUser();
        return new SellerResponseDto(
                seller.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getSurname(),
                seller.getBusinessName(),
                seller.getBusinessEmail(),
                seller.getBusinessPhone(),
                seller.getBusinessAddress(),
                seller.getTaxId(),
                seller.getIdCardNumber(),
                seller.getBankAccountName(),
                seller.getBankAccountNumber(),
                seller.getBankName(),
                seller.getVerificationStatus(),
                seller.getVerificationNotes(),
                seller.getVerifiedAt(),
                seller.getCreatedAt(),
                seller.getIsActive()
        );
    }
}