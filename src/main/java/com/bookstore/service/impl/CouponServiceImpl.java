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
            BigDecimal deficit = coupon.getMinOrderAmount().subtract(applicableSubtotal);
            String categoryName = coupon.isShippingCoupon() ? "miễn phí vận chuyển" : "giảm giá";
            return new CouponValidateResponseDto(
                    false,
                    "Chưa đạt điều kiện: Đơn hàng cần tối thiểu " + formatVnd(coupon.getMinOrderAmount()) +
                            " để nhận ưu đãi " + categoryName + " (Hiện tại: " + formatVnd(applicableSubtotal) +
                            ", mua thêm " + formatVnd(deficit) + ")",
                    BigDecimal.ZERO
            );
        }

        // Phân biệt 2 loại mã: SHIPPING (Vận chuyển) vs ORDER_DISCOUNT (Đơn hàng)
        String category = coupon.getCouponCategory();
        String resolvedType = coupon.getDiscountType() != null ? coupon.getDiscountType().toUpperCase() : "FIXED_AMOUNT";
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscountAmount = BigDecimal.ZERO;

        if ("SHIPPING".equalsIgnoreCase(category) || "FREESHIP".equalsIgnoreCase(resolvedType) || "SHIP_DISCOUNT".equalsIgnoreCase(resolvedType)) {
            category = "SHIPPING";
            BigDecimal standardFee = new BigDecimal("25000");

            if ("FREESHIP".equalsIgnoreCase(resolvedType) || (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) == 0)) {
                resolvedType = "FREESHIP";
                shippingDiscountAmount = standardFee;
            } else {
                resolvedType = "SHIP_DISCOUNT";
                shippingDiscountAmount = coupon.getDiscountValue();
            }

            // Giới hạn giảm ship tối đa nếu có
            if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                shippingDiscountAmount = shippingDiscountAmount.min(coupon.getMaxDiscountAmount());
            }

            discountAmount = BigDecimal.ZERO; // Không giảm trực tiếp trên tiền hàng
            String successMsg = "Áp dụng mã vận chuyển [" + coupon.getCode() + "] thành công: Giảm " + formatVnd(shippingDiscountAmount) + " phí ship!";
            return new CouponValidateResponseDto(true, successMsg, coupon.getCode(), category, discountAmount, resolvedType, shippingDiscountAmount, coupon.getMinOrderAmount());

        } else {
            category = "ORDER_DISCOUNT";
            if ("PERCENT".equalsIgnoreCase(resolvedType)) {
                BigDecimal percent = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
                discountAmount = applicableSubtotal.multiply(percent).divide(new BigDecimal(100));
                // Áp dụng mức giảm tối đa
                if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                }
            } else {
                resolvedType = "FIXED_AMOUNT";
                discountAmount = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
                if (discountAmount.compareTo(applicableSubtotal) > 0) {
                    discountAmount = applicableSubtotal; // Không giảm quá số tiền hàng
                }
            }

            String successMsg = "Áp dụng mã giảm giá đơn hàng [" + coupon.getCode() + "] thành công: Giảm " + formatVnd(discountAmount) + " tiền sách!";
            return new CouponValidateResponseDto(true, successMsg, coupon.getCode(), category, discountAmount, resolvedType, BigDecimal.ZERO, coupon.getMinOrderAmount());
        }
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,d₫", amount.longValue()).replace(',', '.');
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