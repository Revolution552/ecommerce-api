package com.backend.ecommerce.order.service;

import com.backend.ecommerce.order.dao.OrderDAO;
import com.backend.ecommerce.order.model.Order;
import com.backend.ecommerce.order.model.OrderItem;
import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.payload.OrderDTO;
import com.backend.ecommerce.order.payload.OrderItemDTO;
import com.backend.ecommerce.order.payload.OrderRequestDTO; // New import
import com.backend.ecommerce.payments.PaymentRequestDTO;
import com.backend.ecommerce.payments.thewallet.payload.TheWalletInitiateResponse;
import com.backend.ecommerce.payments.thewallet.service.TheWalletService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing Order-related business logic.
 * Handles operations like creating, retrieving, updating, and deleting orders,
 * and performs mapping between DTOs and models.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderDAO orderDAO;
    private final PaymentService paymentService; // Assuming this is your existing PayPal service
    private final TheWalletService theWalletService; // New service for TheWallet integration

    @Autowired
    public OrderService(OrderDAO orderDAO, PaymentService paymentService, TheWalletService theWalletService) {
        this.orderDAO = orderDAO;
        this.paymentService = paymentService;
        this.theWalletService = theWalletService;
    }

    /**
     * Creates a new order and initiates payment based on the provided DTOs.
     *
     * @param orderRequestDTO The OrderRequestDTO containing order and payment details.
     * @return The created OrderDTO with generated ID and status.
     * @throws IllegalArgumentException if order items are empty or invalid.
     * @throws RuntimeException if payment processing fails.
     */
    @Transactional
    public OrderDTO createOrder(OrderRequestDTO orderRequestDTO) {
        OrderDTO orderDTO = orderRequestDTO.getOrderDetails();
        PaymentRequestDTO paymentRequestDTO = orderRequestDTO.getPaymentDetails();

        if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        BigDecimal calculatedTotalAmount = orderDTO.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create the order with PENDING status initially
        Order order = Order.builder()
                .userId(orderDTO.getUserId())
                .totalAmount(calculatedTotalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus("PENDING") // Set initial payment status to PENDING
                .build();

        List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(itemDTO -> OrderItem.builder()
                        .productId(itemDTO.getProductId())
                        .quantity(itemDTO.getQuantity())
                        .price(itemDTO.getPrice())
                        .build())
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        Order savedOrder = orderDAO.save(order); // Save the order to get an ID

        // --- Payment Integration Step ---
        try {
            switch (paymentRequestDTO.getPaymentMethod().toUpperCase()) {
                case "PAYPAL":
                    log.info("Attempting PayPal payment for Order ID: {} with amount: {}", savedOrder.getId(), savedOrder.getTotalAmount());
                    boolean paymentSuccessful = paymentService.processPayment(savedOrder.getId(), savedOrder.getTotalAmount());
                    if (paymentSuccessful) {
                        savedOrder.setStatus(OrderStatus.PROCESSING);
                        savedOrder.setPaymentStatus("PAID");
                    } else {
                        savedOrder.setStatus(OrderStatus.CANCELLED);
                        savedOrder.setPaymentStatus("FAILED");
                        throw new RuntimeException("PayPal payment failed for order ID: " + savedOrder.getId());
                    }
                    break;

                case "THEWALLET":
                    log.info("Attempting TheWallet payment for Order ID: {} with amount: {} via channel: {}",
                            savedOrder.getId(), savedOrder.getTotalAmount(), paymentRequestDTO.getChannel());
                    TheWalletInitiateResponse walletResponse = theWalletService.initiatePushUssd(
                            paymentRequestDTO.getMsisdn(),
                            savedOrder.getTotalAmount().toPlainString(),
                            paymentRequestDTO.getChannel(),
                            paymentRequestDTO.getTarget(),
                            savedOrder.getId().toString()
                    ).block(); // Block and wait for the synchronous response

                    if (walletResponse != null && Boolean.TRUE.equals(walletResponse.getSuccess())) {
                        savedOrder.setStatus(OrderStatus.PROCESSING);
                        savedOrder.setPaymentStatus("PENDING_CALLBACK"); // Awaiting callback confirmation
                        log.info("TheWallet Push USSD initiated successfully for order {}. TheWallet Ref: {}",
                                savedOrder.getId(), walletResponse.getReference());
                    } else {
                        String errorMessage = walletResponse != null && walletResponse.getError() != null ?
                                walletResponse.getError().getMessage() : "Unknown error from TheWallet.";
                        savedOrder.setStatus(OrderStatus.CANCELLED);
                        savedOrder.setPaymentStatus("FAILED_INITIATION");
                        log.error("TheWallet Push USSD initiation failed for order {}: {}", savedOrder.getId(), errorMessage);
                        throw new RuntimeException("TheWallet payment initiation failed: " + errorMessage);
                    }
                    break;

                default:
                    log.warn("Unsupported payment method requested: {}", paymentRequestDTO.getPaymentMethod());
                    savedOrder.setStatus(OrderStatus.CANCELLED);
                    savedOrder.setPaymentStatus("FAILED_UNSUPPORTED_METHOD");
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentRequestDTO.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("Error processing payment for order ID: {}. Exception: {}", savedOrder.getId(), e.getMessage(), e);
            savedOrder.setStatus(OrderStatus.CANCELLED);
            savedOrder.setPaymentStatus("FAILED");
            orderDAO.save(savedOrder);
            throw new RuntimeException("Error processing payment for order ID: " + savedOrder.getId(), e);
        }
        // --- End Payment Integration Step ---

        Order finalOrder = orderDAO.save(savedOrder);
        log.info("Order created successfully with ID: {}", finalOrder.getId());
        return convertToDTO(finalOrder);
    }

    /**
     * Retrieves an order by its ID.
     * @param id The Long ID of the order.
     * @return An Optional containing the OrderDTO if found, or empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDTO> getOrderById(Long id) {
        return orderDAO.findById(id).map(this::convertToDTO);
    }

    /**
     * Retrieves all orders for a specific user.
     * @param userId The Long ID of the user.
     * @return A list of OrderDTOs associated with the given user ID.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderDAO.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an existing order.
     * @param id The Long ID of the order to update.
     * @param newStatus The new status for the order.
     * @return The updated OrderDTO.
     * @throws EntityNotFoundException if the order with the given ID is not found.
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + id));

        order.setStatus(newStatus);
        Order updatedOrder = orderDAO.save(order);
        return convertToDTO(updatedOrder);
    }

    /**
     * Deletes an order by its ID.
     * @param id The Long ID of the order to delete.
     * @throws EntityNotFoundException if the order with the given ID is not found.
     */
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderDAO.existsById(id)) {
            throw new EntityNotFoundException("Order not found with ID: " + id);
        }
        orderDAO.deleteById(id);
    }

    /**
     * Converts an Order model to an OrderDTO.
     * @param order The Order model to convert.
     * @return The corresponding OrderDTO.
     */
    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderItems(itemDTOs)
                .build();
    }
}