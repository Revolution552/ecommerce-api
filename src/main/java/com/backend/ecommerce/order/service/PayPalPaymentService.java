// src/main/java/com/backend/ecommerce/order/service/PayPalPaymentService.java
package com.backend.ecommerce.order.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.math.BigDecimal;
import java.math.RoundingMode; // Import RoundingMode
import java.util.ArrayList;
import java.util.List;
// Removed: import com.paypal.api.payments.Link; // Explicitly import Link - No longer needed with direct iteration

/**
 * Implementation of PaymentService for PayPal using the PayPal REST API SDK.
 * This class handles actual API calls to PayPal for payment processing.
 */
@Service
public class PayPalPaymentService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalPaymentService.class); // Logger instance

    // Inject PayPal API credentials from application.properties or application.yml
    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}") // e.g., "sandbox" or "live"
    private String mode;

    /**
     * Processes a payment for a given order using the PayPal REST API.
     *
     * @param orderId The ID of the order for which payment is being processed.
     * @param amount The total amount to be paid.
     * @return true if the payment was successful, false otherwise.
     * @throws PayPalRESTException if an error occurs during PayPal API interaction.
     * @throws Exception for other general errors.
     */
    @Override
    public boolean processPayment(Long orderId, BigDecimal amount) throws Exception { // Ensure orderId is Long
        logger.info("Attempting PayPal payment for Order ID: {} with amount: {}", orderId, amount);

        // Ensure amount is positive for a valid payment
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Payment amount must be positive for Order ID: {}. Aborting payment.", orderId);
            return false;
        }

        // Configure APIContext with your PayPal credentials and mode
        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        logger.debug("PayPal APIContext initialized for mode: {}", mode);

        // Build the Payer object (e.g., using PayPal account or credit card)
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        logger.debug("Payer set with payment method: {}", payer.getPaymentMethod());

        // Build the RedirectUrls for success and cancel
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:8080/payment/cancel?orderId=" + orderId);
        redirectUrls.setReturnUrl("http://localhost:8080/payment/success?orderId=" + orderId);
        logger.debug("Redirect URLs set: Cancel={}, Return={}", redirectUrls.getCancelUrl(), redirectUrls.getReturnUrl());

        // Build the Amount object
        Amount paypalAmount = new Amount();
        paypalAmount.setCurrency("USD"); // Or your desired currency (e.g., "EUR", "GBP")
        paypalAmount.setTotal(amount.setScale(2, RoundingMode.HALF_UP).toString());
        logger.debug("PayPal amount set: Currency={}, Total={}", paypalAmount.getCurrency(), paypalAmount.getTotal());

        // Build the Transaction object
        Transaction transaction = new Transaction();
        transaction.setAmount(paypalAmount);
        transaction.setDescription("Payment for Order ID: " + orderId);
        transaction.setInvoiceNumber(orderId.toString());
        logger.debug("Transaction details: Description={}, InvoiceNumber={}", transaction.getDescription(), transaction.getInvoiceNumber());

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Build the Payment object
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);
        logger.debug("Payment object built with intent: {}", payment.getIntent());

        try {
            // Create the payment on PayPal
            logger.info("Creating PayPal payment for Order ID: {}", orderId);
            Payment createdPayment = payment.create(apiContext);
            logger.info("PayPal payment creation successful. Payment ID: {}", createdPayment.getId());

            String approvalUrl = null;
            for (com.paypal.api.payments.Links link : createdPayment.getLinks()) {
                if ("approval_url".equals(link.getRel())) {
                    approvalUrl = link.getHref();
                    break;
                }
            }

            if (approvalUrl == null) {
                logger.error("No approval URL found for PayPal payment for Order ID: {}", orderId);
                throw new RuntimeException("No approval URL found for PayPal payment.");
            }

            logger.info("PayPal payment initiated. Approval URL: {}", approvalUrl);
            return true;

        } catch (PayPalRESTException e) {
            logger.error("Error creating PayPal payment for Order ID: {}: {}", orderId, e.getMessage(), e);
            logger.error("PayPal Error Response (details): {}", e.getDetails());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during PayPal payment processing for Order ID: {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
}
