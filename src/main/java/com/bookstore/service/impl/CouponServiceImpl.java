package com.bookstore.service.impl;

import com.bookstore.dto.CouponValidateRequestDto;
import com.bookstore.dto.CouponValidateResponseDto;
import com.bookstore.entity.Coupon;
import com.bookstore.repository.CouponRepository;
import com.bookstore.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public CouponValidateResponseDto applyCoupon(CouponValidateRequestDto request) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(request.getCode())
                .orElse(null);

        if (coupon == null) {
            return new CouponValidateResponseDto(false, "Mã giảm giá không tồn tại hoặc đã bị khóa", BigDecimal.ZERO);
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            return new CouponValidateResponseDto(false, "Mã giảm giá chưa đến thời gian áp dụng", BigDecimal.ZERO);
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            return new CouponValidateResponseDto(false, "Mã giảm giá đã hết hạn", BigDecimal.ZERO);
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return new CouponValidateResponseDto(false, "Mã giảm giá đã hết lượt sử dụng", BigDecimal.ZERO);
        }

        // Kiểm tra điều kiện chi nhánh (FR-CPN-03)
        if (coupon.getApplicableBranch() != null && 
            !coupon.getApplicableBranch().getId().equals(request.getCheckoutBranchId())) {
            return new CouponValidateResponseDto(false, "Mã giảm giá không áp dụng cho chi nhánh này", BigDecimal.ZERO);
        }

        // Tính tổng tiền các sản phẩm ĐỦ ĐIỀU KIỆN áp dụng (FR-CPN-02 & FR-CPN-04)
        BigDecimal applicableSubtotal = BigDecimal.ZERO;
        for (CouponValidateRequestDto.CartItemDto item : request.getCartItems()) {
            if (coupon.getApplicableConditionGrade() == null || 
                coupon.getApplicableConditionGrade().equals(item.getConditionGrade())) {
                
                BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                applicableSubtotal = applicableSubtotal.add(itemTotal);
            }
        }

        if (applicableSubtotal.compareTo(BigDecimal.ZERO) == 0) {
            return new CouponValidateResponseDto(false, "Giỏ hàng không có sách thỏa mãn điều kiện độ cũ của mã", BigDecimal.ZERO);
        }

        if (coupon.getMinOrderAmount() != null && applicableSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return new CouponValidateResponseDto(false, "Chưa đạt giá trị đơn hàng tối thiểu để dùng mã", BigDecimal.ZERO);
        }

        // Tính số tiền giảm
        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("PERCENT".equals(coupon.getDiscountType())) {
            discountAmount = applicableSubtotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal(100));
            // Áp dụng mức giảm tối đa
            if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discountAmount = coupon.getMaxDiscountAmount();
            }
        } else if ("FIXED_AMOUNT".equals(coupon.getDiscountType())) {
            discountAmount = coupon.getDiscountValue();
            if (discountAmount.compareTo(applicableSubtotal) > 0) {
                discountAmount = applicableSubtotal; // Không giảm quá số tiền hàng
            }
        }

        return new CouponValidateResponseDto(true, "Áp dụng mã thành công", discountAmount);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incrementCouponUsage(String code) {
        couponRepository.findByCodeAndIsActiveTrue(code).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }
}