package com.bookstore.service;

import com.bookstore.dto.CouponValidateRequestDto;
import com.bookstore.dto.CouponValidateResponseDto;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Coupon;
import com.bookstore.repository.CouponRepository;
import com.bookstore.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon percentCoupon;
    private Coupon fixedCoupon;
    private Branch sampleBranch;

    @BeforeEach
    void setUp() {
        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "120 Nguyễn Huệ", "028.3822.1101", "Hồ Chí Minh", null, null, true);

        // Mã XACHO50: Giảm 50% cho sách độ cũ ACCEPTABLE, đơn tối thiểu 100k, giảm tối đa 50k
        percentCoupon = new Coupon();
        percentCoupon.setId(1L);
        percentCoupon.setCode("XACHO50");
        percentCoupon.setDiscountType("PERCENT");
        percentCoupon.setDiscountValue(new BigDecimal("50"));
        percentCoupon.setMinOrderAmount(new BigDecimal("100000"));
        percentCoupon.setMaxDiscountAmount(new BigDecimal("50000"));
        percentCoupon.setApplicableConditionGrade("ACCEPTABLE");
        percentCoupon.setApplicableBranch(null); // Áp dụng toàn chuỗi
        percentCoupon.setUsageLimit(100);
        percentCoupon.setUsedCount(5);
        percentCoupon.setStartDate(LocalDateTime.now().minusDays(5));
        percentCoupon.setEndDate(LocalDateTime.now().plusDays(20));
        percentCoupon.setIsActive(true);

        // Mã KHAITRUONG_Q1: Giảm 20k cố định, đơn tối thiểu 80k, chỉ áp dụng tại Chi nhánh 1
        fixedCoupon = new Coupon();
        fixedCoupon.setId(2L);
        fixedCoupon.setCode("KHAITRUONG_Q1");
        fixedCoupon.setDiscountType("FIXED_AMOUNT");
        fixedCoupon.setDiscountValue(new BigDecimal("20000"));
        fixedCoupon.setMinOrderAmount(new BigDecimal("80000"));
        fixedCoupon.setMaxDiscountAmount(null);
        fixedCoupon.setApplicableConditionGrade(null); // Mọi độ cũ
        fixedCoupon.setApplicableBranch(sampleBranch);
        fixedCoupon.setUsageLimit(50);
        fixedCoupon.setUsedCount(0);
        fixedCoupon.setStartDate(LocalDateTime.now().minusDays(1));
        fixedCoupon.setEndDate(LocalDateTime.now().plusDays(10));
        fixedCoupon.setIsActive(true);
    }

    @Test
    @DisplayName("FR-CPN-01: Mã không tồn tại hoặc bị khóa -> Trả về lỗi không hợp lệ")
    void testApplyCoupon_NotFound() {
        CouponValidateRequestDto request = new CouponValidateRequestDto();
        request.setCode("INVALID_CODE");

        when(couponRepository.findByCodeAndIsActiveTrue("INVALID_CODE")).thenReturn(Optional.empty());

        CouponValidateResponseDto response = couponService.applyCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("không tồn tại hoặc đã bị khóa"));
    }

    @Test
    @DisplayName("FR-CPN-02 & FR-CPN-04: Áp dụng mã thành công theo % và kiểm tra đúng độ cũ ACCEPTABLE")
    void testApplyCoupon_PercentAndConditionGrade_Success() {
        CouponValidateRequestDto request = new CouponValidateRequestDto();
        request.setCode("XACHO50");
        request.setCheckoutBranchId(1L);

        // Giỏ hàng gồm 1 sách ACCEPTABLE (120k) và 1 sách LIKE_NEW (80k)
        CouponValidateRequestDto.CartItemDto item1 = new CouponValidateRequestDto.CartItemDto();
        item1.setPrice(new BigDecimal("120000"));
        item1.setQuantity(1);
        item1.setConditionGrade("ACCEPTABLE");

        CouponValidateRequestDto.CartItemDto item2 = new CouponValidateRequestDto.CartItemDto();
        item2.setPrice(new BigDecimal("80000"));
        item2.setQuantity(1);
        item2.setConditionGrade("LIKE_NEW");

        request.setCartItems(java.util.Arrays.asList(item1, item2));

        when(couponRepository.findByCodeAndIsActiveTrue("XACHO50")).thenReturn(Optional.of(percentCoupon));

        CouponValidateResponseDto response = couponService.applyCoupon(request);

        assertTrue(response.isValid());
        // 50% của 120.000 = 60.000, nhưng trần tối đa là 50.000 -> Giảm 50.000đ
        assertEquals(new BigDecimal("50000"), response.getDiscountAmount());
    }

    @Test
    @DisplayName("FR-CPN-02: Giỏ hàng không có sản phẩm khớp với độ cũ yêu cầu -> Báo lỗi")
    void testApplyCoupon_MismatchedConditionGrade() {
        CouponValidateRequestDto request = new CouponValidateRequestDto();
        request.setCode("XACHO50");

        CouponValidateRequestDto.CartItemDto item = new CouponValidateRequestDto.CartItemDto();
        item.setPrice(new BigDecimal("150000"));
        item.setQuantity(1);
        item.setConditionGrade("LIKE_NEW"); // Không phải ACCEPTABLE
        request.setCartItems(Collections.singletonList(item));

        when(couponRepository.findByCodeAndIsActiveTrue("XACHO50")).thenReturn(Optional.of(percentCoupon));

        CouponValidateResponseDto response = couponService.applyCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Giỏ hàng không có sách thỏa mãn điều kiện độ cũ"));
    }

    @Test
    @DisplayName("FR-CPN-03: Kiểm tra ràng buộc chi nhánh áp dụng -> Báo lỗi khi mua ở chi nhánh khác")
    void testApplyCoupon_MismatchedBranch() {
        CouponValidateRequestDto request = new CouponValidateRequestDto();
        request.setCode("KHAITRUONG_Q1");
        request.setCheckoutBranchId(2L); // Chi nhánh 2 (mã chỉ áp dụng chi nhánh 1)

        when(couponRepository.findByCodeAndIsActiveTrue("KHAITRUONG_Q1")).thenReturn(Optional.of(fixedCoupon));

        CouponValidateResponseDto response = couponService.applyCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("không áp dụng cho chi nhánh này"));
    }

    @Test
    @DisplayName("FR-CPN-04: Chưa đạt giá trị đơn hàng tối thiểu -> Báo lỗi")
    void testApplyCoupon_MinOrderAmountNotReached() {
        CouponValidateRequestDto request = new CouponValidateRequestDto();
        request.setCode("KHAITRUONG_Q1");
        request.setCheckoutBranchId(1L);

        // Đơn hàng chỉ có 50.000đ (< 80.000đ)
        CouponValidateRequestDto.CartItemDto item = new CouponValidateRequestDto.CartItemDto();
        item.setPrice(new BigDecimal("50000"));
        item.setQuantity(1);
        item.setConditionGrade("GOOD");
        request.setCartItems(Collections.singletonList(item));

        when(couponRepository.findByCodeAndIsActiveTrue("KHAITRUONG_Q1")).thenReturn(Optional.of(fixedCoupon));

        CouponValidateResponseDto response = couponService.applyCoupon(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Chưa đạt giá trị đơn hàng tối thiểu"));
    }

    @Test
    @DisplayName("Tăng số lượt đã sử dụng coupon thành công")
    void testIncrementCouponUsage() {
        when(couponRepository.findByCodeAndIsActiveTrue("XACHO50")).thenReturn(Optional.of(percentCoupon));

        couponService.incrementCouponUsage("XACHO50");

        assertEquals(6, percentCoupon.getUsedCount());
        verify(couponRepository, times(1)).save(percentCoupon);
    }
}
