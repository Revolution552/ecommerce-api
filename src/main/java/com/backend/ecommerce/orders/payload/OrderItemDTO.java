package com.backend.ecommerce.orders.payload;

public class OrderItemDTO {
    private Long productId;
    private int quantity;
    private Double price;

    public OrderItemDTO(Long productId, int quantity, Double price) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be a non-negative value");
        }

        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must be a non-negative value");
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
