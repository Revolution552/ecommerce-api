package com.backend.ecommerce.payment.controller;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.service.OrderService;
import com.backend.ecommerce.payment.service.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PayPalController {

    private static final Logger logger = LoggerFactory.getLogger(PayPalController.class);

    @Autowired
    private PayPalService paypalService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<?> payForOrder(@PathVariable("orderId") Long orderId) {
        logger.info("Initiating payment for order ID: {}", orderId);
        Optional<Order> orderOpt = orderService.findOrderById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found for ID: {}", orderId);
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }

        Order order = orderOpt.get();

        // Validate total price is greater than zero
        if (order.getTotalAmount() <= 0) {
            logger.warn("Order total price must be greater than zero for order ID: {}", orderId);
            return ResponseEntity.status(400).body(Map.of("error", "Order total price must be greater than zero"));
        }

        try {
            Payment payment = paypalService.createPayment(
                    order.getTotalAmount(),  // Ensure this method exists and returns the correct total
                    "USD",  // currency
                    "paypal",
                    "sale",
                    "Order Payment for Order ID: " + orderId,
                    "http://localhost:8080/payment/cancel",
                    "http://localhost:8080/payment/success"
            );

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    logger.info("Payment created successfully for order ID: {}. Redirecting to: {}", orderId, link.getHref());
                    return ResponseEntity.ok(Map.of("redirect_url", link.getHref()));
                }
            }
        } catch (PayPalRESTException e) {
            logger.error("Payment creation failed for order ID: {}. Error: {}", orderId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Payment failed", "details", e.getMessage()));
        }

        logger.error("Payment creation failed for order ID: {}", orderId);
        return ResponseEntity.status(500).body(Map.of("error", "Payment creation failed"));
    }

    // Define a DTO for the payment success request
    public static class PaymentSuccessRequest {
        private String paymentId;
        private String payerId;

        // Getters and Setters
        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public String getPayerId() {
            return payerId;
        }

        public void setPayerId(String payerId) {
            this.payerId = payerId;
        }
    }

    @GetMapping("/success")
    public ResponseEntity<?> successPay(@RequestBody PaymentSuccessRequest request) {
        String paymentId = request.getPaymentId();
        String payerId = request.getPayerId();

        logger.info("Payment successful. Payment ID: {}, Payer ID: {}", paymentId, payerId);
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                // Extract orderId from the payment details
                Long orderId = Long.parseLong(payment.getTransactions().get(0).getInvoiceNumber());
                logger.info("Updating order status to PAID for order ID: {}", orderId);
                orderService.updateOrderStatus(orderId, OrderStatus.PAID, /*user*/ null); // You need to pass the actual LocalUser instance
                return ResponseEntity.ok(Map.of("message", "Payment successful", "payment", payment));
            }
        } catch (PayPalRESTException e) {
            logger.error("Payment execution failed. Payment ID: {}, Error: {}", paymentId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Payment execution failed", "details", e.getMessage()));
        }

        logger.error("Payment execution failed for Payment ID: {}", paymentId);
        return ResponseEntity.status(500).body(Map.of("error", "Payment execution failed"));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancelPay() {
        logger.info("Payment cancelled by the user.");
        return ResponseEntity.ok(Map.of("message", "Payment cancelled"));
    }
}
