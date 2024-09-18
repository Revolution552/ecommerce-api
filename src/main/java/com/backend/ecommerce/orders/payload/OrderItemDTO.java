package com.backend.ecommerce.orders.payload;

public class OrderItemDTO {
    private String productName;
    private int quantity;
    private Double price;

    public OrderItemDTO(String productName, int quantity, Double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    // Constructor, getters, and setters
}
