package com.backend.ecommerce.payment.controller;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.service.OrderService;
import com.backend.ecommerce.payment.service.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PayPalController {

    @Autowired
    private PayPalService paypalService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<?> payForOrder(@PathVariable("orderId") Long orderId) {
        Optional<Order> orderOpt = orderService.findOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }

        Order order = orderOpt.get();

        // Validate total price is greater than zero
        if (order.getTotalPrice() <= 0) {
            return ResponseEntity.status(400).body(Map.of("error", "Order total price must be greater than zero"));
        }

        try {
            Payment payment = paypalService.createPayment(
                    order.getTotalPrice(),  // Ensure this method exists and returns the correct total
                    "USD",  // currency
                    "paypal",
                    "sale",
                    "Order Payment for Order ID: " + orderId,
                    "http://localhost:8080/payment/cancel",
                    "http://localhost:8080/payment/success"
            );

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return ResponseEntity.ok(Map.of("redirect_url", link.getHref()));
                }
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Payment failed", "details", e.getMessage()));
        }

        return ResponseEntity.status(500).body(Map.of("error", "Payment creation failed"));
    }


    @GetMapping("/success")
    public ResponseEntity<?> successPay(@RequestParam("paymentId") String paymentId,
                                        @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                // Extract orderId from the payment details
                Long orderId = Long.parseLong(payment.getTransactions().get(0).getInvoiceNumber());

                orderService.updateOrderStatus(orderId, OrderStatus.PAID, /*user*/ null); // You need to pass the actual LocalUser instance

                return ResponseEntity.ok(Map.of("message", "Payment successful", "payment", payment));
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Payment execution failed", "details", e.getMessage()));
        }

        return ResponseEntity.status(500).body(Map.of("error", "Payment execution failed"));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancelPay() {
        return ResponseEntity.ok(Map.of("message", "Payment cancelled"));
    }
}
