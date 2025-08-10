package com.backend.ecommerce.payments.dao;

import com.backend.ecommerce.payments.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface PaymentDAO extends JpaRepository<Payment, Long> {

    /**
     * Finds a Payment record by the ID of the associated Order.
     * @param orderId The ID of the Order.
     * @return An Optional containing the Payment if found, or empty otherwise.
     */
    Optional<Payment> findByOrder_Id(Long orderId);

    /**
     * Finds a Payment record by the transaction ID provided by the mobile money provider.
     * This is useful for looking up payments based on callback data.
     * @param providerTransactionId The transaction ID from the mobile money provider.
     * @return An Optional containing the Payment if found, or empty otherwise.
     */
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
}