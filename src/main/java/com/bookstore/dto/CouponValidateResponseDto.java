package com.bookstore.dto;

import java.math.BigDecimal;

public class CouponValidateResponseDto {
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;

    public CouponValidateResponseDto(boolean valid, String message, BigDecimal discountAmount) {
        this.valid = valid;
        this.message = message;
        this.discountAmount = discountAmount;
    }

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}
}