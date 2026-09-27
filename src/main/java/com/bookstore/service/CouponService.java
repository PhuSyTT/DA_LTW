package com.bookstore.service;

import com.bookstore.dto.CouponValidateRequestDto;
import com.bookstore.dto.CouponValidateResponseDto;

public interface CouponService {
    CouponValidateResponseDto applyCoupon(CouponValidateRequestDto request);
    void incrementCouponUsage(String code);
}