package com.backend.ecommerce.user.repository;

import com.backend.ecommerce.user.model.PasswordResetToken;
import com.backend.ecommerce.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // NEW/UPDATED: Ensure this method is transactional and modifying to perform deletion
    @Modifying // Indicates that this query is a modifying query (e.g., INSERT, UPDATE, DELETE)
    @Transactional // Ensures the deletion operation is performed within a transaction
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user") // Explicit JPQL query for deletion
    void deleteByUser(User user);
}
