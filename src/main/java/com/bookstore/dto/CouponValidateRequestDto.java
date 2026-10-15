package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class CouponValidateRequestDto {
    private String code;
    private Long checkoutBranchId;
    private List<CartItemDto> cartItems;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCheckoutBranchId() {
        return checkoutBranchId;
    }

    public void setCheckoutBranchId(Long checkoutBranchId) {
        this.checkoutBranchId = checkoutBranchId;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    // Class con lưu chi tiết giỏ hàng
    public static class CartItemDto {
        private BigDecimal price;
        private String conditionGrade;
        private int quantity;
        
        public BigDecimal getPrice() {
            return price;
        }
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        public String getConditionGrade() {
            return conditionGrade;
        }
        public void setConditionGrade(String conditionGrade) {
            this.conditionGrade = conditionGrade;
        }
        public int getQuantity() {
            return quantity;
        }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}