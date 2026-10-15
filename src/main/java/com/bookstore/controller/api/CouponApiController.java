package com.bookstore.controller.api;

import com.bookstore.dto.CouponValidateRequestDto;
import com.bookstore.dto.CouponValidateResponseDto;
import com.bookstore.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponApiController {

    private final CouponService couponService;

    public CouponApiController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/apply")
    public ResponseEntity<CouponValidateResponseDto> applyCoupon(@RequestBody CouponValidateRequestDto request) {
        CouponValidateResponseDto response = couponService.applyCoupon(request);
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}