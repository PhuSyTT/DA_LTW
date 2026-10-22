package com.bookstore.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDto> items = new ArrayList<>();
    private String couponCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;

    public CartDto() {
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public BigDecimal getShippingFee() {
        return shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotal() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal discount = getDiscountAmount();
        BigDecimal shipping = getShippingFee();

        BigDecimal total = subtotal.subtract(discount).add(shipping);
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSavingsPotential() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItemDto::getPotentialSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasSavingsAvailable() {
        return getTotalSavingsPotential().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}

