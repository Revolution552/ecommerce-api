// src/main/java/com/backend/ecommerce/order/service/OrderService.java
package com.backend.ecommerce.order.service;

import com.backend.ecommerce.cart.exception.CartNotFoundException;
import com.backend.ecommerce.cart.model.Cart;
import com.backend.ecommerce.cart.model.CartItem;
import com.backend.ecommerce.cart.repository.CartRepository;
import com.backend.ecommerce.order.exception.*;
import com.backend.ecommerce.order.model.*;
import com.backend.ecommerce.order.payload.*;
import com.backend.ecommerce.order.repository.OrderItemRepository;
import com.backend.ecommerce.order.repository.OrderRepository;
import com.backend.ecommerce.product.exception.InsufficientStockException;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.repository.ProductRepository;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository;
import com.backend.ecommerce.user.service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService;

    private static final BigDecimal TAX_RATE = BigDecimal.ZERO;
    private static final BigDecimal SHIPPING_COST = BigDecimal.ZERO;
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CartRepository cartRepository,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponseDto createOrder(String userEmail, CreateOrderRequest request)
            throws OrderValidationException, InsufficientStockException {

        logger.info("Creating order for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderValidationException("Order must contain at least one item");
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .currency("USD")
                .customerNotes(request.getCustomerNotes())
                .couponCode(request.getCouponCode())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create shipping address
        OrderShipping shipping = createShipping(savedOrder, request.getShippingAddress());
        savedOrder.setShipping(shipping);

        // Create payment record
        OrderPayment payment = createPayment(savedOrder, request.getPaymentMethod(), savedOrder.getTotal());
        savedOrder.setPayment(payment);

        // Process order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = createOrderItem(savedOrder, itemRequest);
            orderItems.add(orderItem);
        }
        savedOrder.setItems(orderItems);

        // Calculate totals
        savedOrder.setShippingCost(SHIPPING_COST);
        savedOrder.setTax(TAX_RATE.multiply(savedOrder.getSubtotal()));
        savedOrder.calculateTotals();
        savedOrder.getPayment().setAmount(savedOrder.getTotal());

        // Add status history
        savedOrder.addStatusHistory(null, OrderStatus.PENDING, "Order created", user);

        Order finalOrder = orderRepository.save(savedOrder);

        // Clear user's cart
        clearUserCart(user);

        // Send order confirmation email
        try {
            emailService.sendOrderConfirmationEmail(user, finalOrder);
        } catch (EmailFailureException e) {
            logger.error("Failed to send order confirmation email", e);
        }

        logger.info("Order created successfully. Order number: {}", finalOrder.getOrderNumber());
        return mapToResponseDto(finalOrder);
    }

    @Transactional
    public OrderResponseDto createOrderFromCart(String userEmail, CreateOrderFromCartRequest request)
            throws CartNotFoundException, OrderValidationException, InsufficientStockException {

        logger.info("Creating order from cart for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::getIsSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new OrderValidationException("No items selected in cart");
        }

        // Convert cart items to order item requests
        List<OrderItemRequest> itemRequests = selectedItems.stream()
                .map(cartItem -> {
                    OrderItemRequest itemRequest = new OrderItemRequest();
                    itemRequest.setProductId(cartItem.getProduct().getId());
                    itemRequest.setQuantity(cartItem.getQuantity());
                    itemRequest.setNotes(cartItem.getNotes());
                    return itemRequest;
                })
                .collect(Collectors.toList());

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setItems(itemRequests);
        orderRequest.setShippingAddress(request.getShippingAddress());
        orderRequest.setPaymentMethod(request.getPaymentMethod());
        orderRequest.setCouponCode(request.getCouponCode());
        orderRequest.setCustomerNotes(request.getCustomerNotes());
        orderRequest.setShippingMethod(request.getShippingMethod());

        return createOrder(userEmail, orderRequest);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request, User changedBy)
            throws OrderNotFoundException, InvalidOrderStatusTransitionException {

        logger.info("Updating order status for order: {} to {}", orderNumber, request.getStatus());

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));

        validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.getStatus());

        // Update timestamps based on status
        updateOrderTimestamps(order, request.getStatus());

        // Update shipping info if provided
        if (request.getTrackingNumber() != null && order.getShipping() != null) {
            order.getShipping().setTrackingNumber(request.getTrackingNumber());
            order.getShipping().setTrackingUrl(request.getTrackingUrl());
            order.getShipping().setCarrier(request.getCarrier());

            if (request.getStatus() == OrderStatus.SHIPPED) {
                order.getShipping().setShippedAt(LocalDateTime.now());
                order.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
            }
        }

        if (request.getStatus() == OrderStatus.DELIVERED && order.getShipping() != null) {
            order.getShipping().setDeliveredAt(LocalDateTime.now());
            order.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        }

        // Add status history
        order.addStatusHistory(previousStatus, request.getStatus(), request.getNotes(), changedBy);

        Order updatedOrder = orderRepository.save(order);

        // Send status update email
        try {
            emailService.sendOrderStatusUpdateEmail(order.getUser(), updatedOrder);
        } catch (EmailFailureException e) {
            logger.error("Failed to send order status update email", e);
        }

        logger.info("Order status updated successfully for order: {}", orderNumber);
        return mapToResponseDto(updatedOrder);
    }

    @Transactional
    public OrderResponseDto updatePaymentStatus(String orderNumber, UpdatePaymentStatusRequest request)
            throws OrderNotFoundException {

        logger.info("Updating payment status for order: {} to {}", orderNumber, request.getStatus());

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));

        OrderPayment payment = order.getPayment();
        if (payment == null) {
            throw new OrderValidationException("Order has no payment record");
        }

        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(request.getStatus());

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }
        if (request.getPaymentDetails() != null) {
            payment.setPaymentDetails(request.getPaymentDetails());
        }
        if (request.getErrorMessage() != null) {
            payment.setErrorMessage(request.getErrorMessage());
        }

        if (request.getStatus() == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
            order.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);

            // Update order status if payment successful
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.addStatusHistory(OrderStatus.PENDING, OrderStatus.CONFIRMED,
                        "Payment confirmed", null);
            }
        } else if (request.getStatus() == PaymentStatus.FAILED) {
            order.setPaymentStatus(PaymentStatus.FAILED);
        } else if (request.getStatus() == PaymentStatus.REFUNDED) {
            payment.setRefundedAt(LocalDateTime.now());
            order.setRefundedAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
            order.addStatusHistory(order.getStatus(), OrderStatus.REFUNDED,
                    "Payment refunded", null);
        }

        Order updatedOrder = orderRepository.save(order);

        // Send payment confirmation email
        if (request.getStatus() == PaymentStatus.PAID && previousStatus != PaymentStatus.PAID) {
            try {
                emailService.sendPaymentConfirmationEmail(order.getUser(), updatedOrder);
            } catch (EmailFailureException e) {
                logger.error("Failed to send payment confirmation email", e);
            }
        }

        logger.info("Payment status updated successfully for order: {}", orderNumber);
        return mapToResponseDto(updatedOrder);
    }

    @Transactional
    public OrderResponseDto cancelOrder(String orderNumber, String reason, User cancelledBy)
            throws OrderNotFoundException, OrderValidationException {

        logger.info("Cancelling order: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));

        if (!order.canBeCancelled()) {
            throw new OrderValidationException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setFulfillmentStatus(FulfillmentStatus.CANCELLED);

        // Release reserved stock
        for (OrderItem item : order.getItems()) {
            releaseReservedStock(item.getProduct().getId(), item.getQuantity());
        }

        // Add status history
        order.addStatusHistory(previousStatus, OrderStatus.CANCELLED,
                "Order cancelled: " + reason, cancelledBy);

        Order updatedOrder = orderRepository.save(order);

        // Send cancellation email
        try {
            emailService.sendOrderCancellationEmail(order.getUser(), updatedOrder, reason);
        } catch (EmailFailureException e) {
            logger.error("Failed to send order cancellation email", e);
        }

        logger.info("Order cancelled successfully: {}", orderNumber);
        return mapToResponseDto(updatedOrder);
    }

    public OrderResponseDto getOrder(String orderNumber) throws OrderNotFoundException {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return mapToResponseDto(order);
    }

    public OrderResponseDto getOrderById(Long orderId) throws OrderNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
        return mapToResponseDto(order);
    }

    public Page<OrderSummaryDto> getUserOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUser(user, pageable)
                .map(this::mapToSummaryDto);
    }

    public List<OrderSummaryDto> getUserOrdersByStatus(String userEmail, OrderStatus status) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserIdAndStatus(user.getId(), status).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    public Page<OrderResponseDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDto);
    }

    public Page<OrderResponseDto> searchOrders(String keyword, Pageable pageable) {
        return orderRepository.searchOrders(keyword, pageable)
                .map(this::mapToResponseDto);
    }

    public List<OrderResponseDto> getShopOrders(Long shopId) {
        return orderItemRepository.findByShopId(shopId, Pageable.unpaged())
                .map(orderItem -> mapToResponseDto(orderItem.getOrder()))
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public OrderStatsDto getOrderStats(LocalDateTime start, LocalDateTime end) {
        Object[] stats = orderRepository.getOrderStatsForPeriod(start, end);

        Long totalOrders = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        BigDecimal totalRevenue = stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;

        return new OrderStatsDto(totalOrders, totalRevenue, start, end);
    }

    public ShopOrderStatsDto getShopOrderStats(Long shopId, LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = orderItemRepository.calculateRevenueByShopForPeriod(shopId, start, end);
        Long totalOrders = orderItemRepository.findByShopId(shopId, Pageable.unpaged())
                .map(orderItem -> orderItem.getOrder().getId())
                .stream()
                .distinct()
                .count();

        return new ShopOrderStatsDto(shopId, totalOrders, revenue, start, end);
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<Order> expiredOrders = orderRepository.findPendingPaymentsOlderThan(cutoffTime);

        for (Order order : expiredOrders) {
            try {
                cancelOrder(order.getOrderNumber(), "Payment timeout - automatically cancelled", null);
                logger.info("Auto-cancelled expired order: {}", order.getOrderNumber());
            } catch (Exception e) {
                logger.error("Failed to auto-cancel order: {}", order.getOrderNumber(), e);
            }
        }
    }

    // Private helper methods
    private OrderShipping createShipping(Order order, ShippingAddressRequest request) {
        return OrderShipping.builder()
                .order(order)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();
    }

    private OrderPayment createPayment(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        return OrderPayment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .currency(order.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest request)
            throws InsufficientStockException {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        // Validate stock
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getName(), product.getQuantity(), request.getQuantity()));
        }

        // Reserve stock
        reserveStock(product.getId(), request.getQuantity());

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .shop(product.getShop())
                .productName(product.getName())
                .productSku(product.getSku())
                .productImage(product.getMainImageUrl())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .notes(request.getNotes())
                .fulfillmentStatus(FulfillmentStatus.PENDING)
                .build();

        orderItem.calculateTotalPrice();
        return orderItemRepository.save(orderItem);
    }

    private void reserveStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setQuantity(product.getQuantity() - quantity);
            product.setReservedQuantity(product.getReservedQuantity() + quantity);
            productRepository.save(product);
        }
    }

    private void releaseReservedStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setQuantity(product.getQuantity() + quantity);
            product.setReservedQuantity(product.getReservedQuantity() - quantity);
            productRepository.save(product);
        }
    }

    private void clearUserCart(User user) {
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setTotalItems(0);
            cartRepository.save(cart);
        });
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to)
            throws InvalidOrderStatusTransitionException {

        Map<OrderStatus, Set<OrderStatus>> allowedTransitions = Map.of(
                OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED, OrderStatus.PAYMENT_FAILED),
                OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
                OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
                OrderStatus.DELIVERED, Set.of(OrderStatus.COMPLETED, OrderStatus.REFUNDED),
                OrderStatus.COMPLETED, Set.of(OrderStatus.REFUNDED)
        );

        Set<OrderStatus> allowed = allowedTransitions.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidOrderStatusTransitionException(
                    String.format("Cannot transition from %s to %s", from, to));
        }
    }

    private void updateOrderTimestamps(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                order.setOrderedAt(LocalDateTime.now());
                break;
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
            case REFUNDED:
                order.setRefundedAt(LocalDateTime.now());
                break;
            default:
                break;
        }
    }

    private OrderResponseDto mapToResponseDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                .map(this::mapToItemResponseDto)
                .collect(Collectors.toList());

        OrderShippingResponseDto shippingDto = null;
        if (order.getShipping() != null) {
            OrderShipping s = order.getShipping();
            shippingDto = new OrderShippingResponseDto(
                    s.getId(), s.getFullName(), s.getEmail(), s.getPhone(),
                    s.getAddressLine1(), s.getAddressLine2(), s.getCity(),
                    s.getState(), s.getPostalCode(), s.getCountry(),
                    s.getShippingMethod(), s.getTrackingNumber(), s.getTrackingUrl(),
                    s.getCarrier(), s.getEstimatedDelivery(), s.getShippedAt(), s.getDeliveredAt()
            );
        }

        OrderPaymentResponseDto paymentDto = null;
        if (order.getPayment() != null) {
            OrderPayment p = order.getPayment();
            paymentDto = new OrderPaymentResponseDto(
                    p.getId(), p.getPaymentMethod(), p.getTransactionId(),
                    p.getAmount(), p.getCurrency(), p.getStatus(),
                    p.getPaymentDetails(), p.getPaidAt(), p.getRefundAmount(), p.getRefundedAt()
            );
        }

        List<OrderStatusHistoryResponseDto> historyDtos = order.getStatusHistory().stream()
                .map(h -> new OrderStatusHistoryResponseDto(
                        h.getId(), h.getPreviousStatus(), h.getNewStatus(),
                        h.getNotes(),
                        h.getChangedBy() != null ?
                                h.getChangedBy().getFirstName() + " " + h.getChangedBy().getSurname() : "System",
                        h.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getUser().getFirstName() + " " + order.getUser().getSurname(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getFulfillmentStatus(),
                itemDtos,
                shippingDto,
                paymentDto,
                historyDtos,
                order.getSubtotal(),
                order.getShippingCost(),
                order.getTax(),
                order.getDiscount(),
                order.getTotal(),
                order.getNotes(),
                order.getCustomerNotes(),
                order.getCouponCode(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getOrderedAt(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt(),
                order.canBeCancelled(),
                order.canBeRefunded()
        );
    }

    private OrderItemResponseDto mapToItemResponseDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getProductSku(),
                item.getProductImage(),
                item.getProduct().getSlug(),
                item.getShop().getId(),
                item.getShop().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getCompareAtPrice(),
                item.getTotalPrice(),
                item.getDiscount(),
                item.getTax(),
                item.getSavings(),
                item.getNotes(),
                item.getFulfillmentStatus()
        );
    }

    private OrderSummaryDto mapToSummaryDto(Order order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getItems().size(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }
}