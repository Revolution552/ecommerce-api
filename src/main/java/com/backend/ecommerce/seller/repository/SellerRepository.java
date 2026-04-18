// src/main/java/com/backend/ecommerce/seller/repository/SellerRepository.java
package com.backend.ecommerce.seller.repository;

import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.seller.model.SellerVerificationStatus;
import com.backend.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(User user);

    Optional<Seller> findByUserId(Long userId);

    List<Seller> findByVerificationStatus(SellerVerificationStatus status);

    boolean existsByUserId(Long userId);

    boolean existsByBusinessEmail(String businessEmail);

    boolean existsByTaxId(String taxId);
}