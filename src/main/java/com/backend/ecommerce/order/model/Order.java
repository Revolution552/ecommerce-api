package com.backend.ecommerce.order.model;

import com.backend.ecommerce.payments.model.Payment; // Import the Payment entity
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Order model in the e-commerce system.
 * This class maps to the 'orders' table in the database.
 * Uses Lombok for boilerplate code (getters, setters, constructors).
 */
@Entity
@Table(name = "orders")
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Generates a unique ID for each order
    private Long id;

    @Column(nullable = false)
    private Long userId; // ID of the user who placed the order

    @CreationTimestamp // Automatically sets the creation timestamp when the order is persisted
    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private BigDecimal totalAmount; // Total amount of the order

    @Enumerated(EnumType.STRING) // Stores the enum name as a string in the database
    @Column(nullable = false)
    private OrderStatus status; // Current status of the order (e.g., PENDING, SHIPPED, DELIVERED)
    // ^^^ Changed the type of 'status' to your custom 'OrderStatus'

    // One order can have many order items.
    // CascadeType.ALL: Operations (persist, merge, remove) on Order will cascade to OrderItem.
    // orphanRemoval = true: If an OrderItem is removed from the list, it will be deleted from the database.
    // mappedBy: Indicates that the 'order' field in OrderItem is the owning side of the relationship.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Ensures that the default value is used when using the builder pattern
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Helper method to add an OrderItem to the order and set its parent order.
     * @param item The OrderItem to add.
     */
    public void addOrderItem(OrderItem item) {
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        orderItems.add(item);
        item.setOrder(this); // Set the parent order for the order item
    }

    /**
     * Helper method to remove an OrderItem from the order.
     * @param item The OrderItem to remove.
     */
    public void removeOrderItem(OrderItem item) {
        if (this.orderItems != null) {
            orderItems.remove(item);
            item.setOrder(null); // Remove the parent order reference
        }
    }

    /**
     * Custom setter for orderItems to ensure bidirectional relationship is maintained.
     * Lombok's @Data will generate a default setter, but we override it here
     * to ensure `item.setOrder(this)` is called for each item.
     * @param orderItems The list of OrderItem entities for this order.
     */
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems.clear(); // Clear existing items to avoid duplicates
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                this.addOrderItem(item); // Use helper method to add and set parent
            }
        }
    }

    @Column(nullable = false)
    @Builder.Default // Lombok annotation to use the default value with the builder
    private String paymentStatus = "PENDING"; // Default value for new orders

    // This is the One-to-one relationship with the Payment entity.
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Payment payment;
}