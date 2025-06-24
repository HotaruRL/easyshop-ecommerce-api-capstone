package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class ShoppingCartItem {
    private Product product = null;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;


    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    @JsonIgnore
    public int getProductId() {
        return this.product.getProductId();
    }

    public BigDecimal getSalesPrice() {
        BigDecimal basePrice = product.getPrice();
        BigDecimal quantity = new BigDecimal(this.quantity);

        return basePrice.multiply(quantity);
    }

    public BigDecimal getDiscount() {
        return getSalesPrice().multiply(discountPercent);
    }

    public BigDecimal getLineTotal() {
        BigDecimal subTotal = getSalesPrice();
        BigDecimal discountAmount = getDiscount();

        return subTotal.subtract(discountAmount);
    }
}
