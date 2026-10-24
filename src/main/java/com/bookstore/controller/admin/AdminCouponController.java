package com.bookstore.controller.admin;

import com.bookstore.entity.Branch;
import com.bookstore.entity.Coupon;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.CouponRepository;
import com.bookstore.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private static final Logger log = LoggerFactory.getLogger(AdminCouponController.class);

    private final CouponRepository couponRepository;
    private final BranchRepository branchRepository;

    public AdminCouponController(CouponRepository couponRepository, BranchRepository branchRepository) {
        this.couponRepository = couponRepository;
        this.branchRepository = branchRepository;
    }

    /**
     * RBAC:
     * - ADMIN: Xem toàn bộ mã giảm giá trên toàn chuỗi, có quyền tạo/sửa/xóa.
     * - BRANCH_MANAGER: Chỉ xem các mã áp dụng cho chi nhánh của mình hoặc toàn chuỗi (Read-only).
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER')")
    public String listCoupons(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        boolean isAdmin = userDetails != null && (
                "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRoleName()) ||
                "ADMIN".equalsIgnoreCase(userDetails.getRoleName())
        );

        List<Coupon> coupons;
        if (isAdmin) {
            coupons = couponRepository.findAllByOrderByIdDesc();
        } else {
            Long branchId = (userDetails != null) ? userDetails.getBranchId() : null;
            if (branchId != null) {
                coupons = couponRepository.findByApplicableBranchIdOrApplicableBranchIsNullOrderByIdDesc(branchId);
            } else {
                coupons = couponRepository.findAllByOrderByIdDesc();
            }
        }

        model.addAttribute("coupons", coupons);
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("userBranchName", userDetails != null ? userDetails.getBranchName() : null);
        model.addAttribute("activeMenu", "coupons");

        return "admin/coupons/list";
    }

    /**
     * Tạo mã giảm giá mới - Chỉ dành cho ADMIN tối cao
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public String createCoupon(
            @RequestParam("code") String code,
            @RequestParam(value = "couponCategory", defaultValue = "ORDER_DISCOUNT") String couponCategory,
            @RequestParam("discountType") String discountType,
            @RequestParam(value = "discountValue", required = false) BigDecimal discountValue,
            @RequestParam(value = "minOrderAmount", required = false) BigDecimal minOrderAmount,
            @RequestParam(value = "maxDiscountAmount", required = false) BigDecimal maxDiscountAmount,
            @RequestParam(value = "applicableConditionGrade", required = false) String applicableConditionGrade,
            @RequestParam(value = "applicableBranchId", required = false) Long applicableBranchId,
            @RequestParam(value = "usageLimit", defaultValue = "100") Integer usageLimit,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String cleanCode = code.trim().toUpperCase();
            if (couponRepository.existsByCode(cleanCode)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá '" + cleanCode + "' đã tồn tại trong hệ thống!");
                return "redirect:/admin/coupons";
            }

            Coupon coupon = new Coupon();
            coupon.setCode(cleanCode);
            coupon.setCouponCategory(couponCategory);
            coupon.setDiscountType(discountType);
            
            // Nếu là FREESHIP mà để trống discountValue, mặc định 25.000₫
            if (discountValue == null) {
                if ("FREESHIP".equalsIgnoreCase(discountType)) {
                    discountValue = new BigDecimal("25000");
                } else {
                    discountValue = BigDecimal.ZERO;
                }
            }
            coupon.setDiscountValue(discountValue);
            coupon.setMinOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO);
            coupon.setMaxDiscountAmount(maxDiscountAmount);

            if (applicableConditionGrade != null && !applicableConditionGrade.isBlank() && !"ALL".equalsIgnoreCase(applicableConditionGrade)) {
                coupon.setApplicableConditionGrade(applicableConditionGrade.trim());
            } else {
                coupon.setApplicableConditionGrade(null);
            }

            if (applicableBranchId != null && applicableBranchId > 0) {
                Branch branch = branchRepository.findById(applicableBranchId).orElse(null);
                coupon.setApplicableBranch(branch);
            } else {
                coupon.setApplicableBranch(null); // Toàn chuỗi
            }

            coupon.setUsageLimit(usageLimit != null ? usageLimit : 100);
            coupon.setUsedCount(0);
            coupon.setIsActive(true);

            LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now();
            LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now().plusMonths(3);
            coupon.setStartDate(startDateTime);
            coupon.setEndDate(endDateTime);

            couponRepository.save(coupon);
            log.info("Admin created new coupon: {}", cleanCode);

            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo mã giảm giá '" + cleanCode + "' thành công!");
        } catch (Exception e) {
            log.error("Error creating coupon: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo mã giảm giá: " + e.getMessage());
        }

        return "redirect:/admin/coupons";
    }

    /**
     * Bật / Tắt trạng thái kích hoạt của mã giảm giá - Chỉ dành cho ADMIN
     */
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public String toggleCouponStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        if (coupon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã giảm giá ID: " + id);
            return "redirect:/admin/coupons";
        }

        boolean newStatus = !Boolean.TRUE.equals(coupon.getIsActive());
        coupon.setIsActive(newStatus);
        couponRepository.save(coupon);

        String statusText = newStatus ? "Kích hoạt" : "Tạm khóa";
        redirectAttributes.addFlashAttribute("successMessage", statusText + " mã giảm giá '" + coupon.getCode() + "' thành công!");
        return "redirect:/admin/coupons";
    }

    /**
     * Xóa mã giảm giá - Chỉ dành cho ADMIN
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public String deleteCoupon(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        if (coupon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã giảm giá ID: " + id);
            return "redirect:/admin/coupons";
        }

        try {
            couponRepository.delete(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá '" + coupon.getCode() + "' thành công!");
        } catch (Exception e) {
            // Trường hợp mã đã được dùng trong đơn hàng cũ, không xóa vật lý mà khóa vĩnh viễn
            coupon.setIsActive(false);
            couponRepository.save(coupon);
            redirectAttributes.addFlashAttribute("warningMessage", "Mã giảm giá đã phát sinh dữ liệu trong các đơn hàng cũ, hệ thống đã chuyển sang trạng thái Vô hiệu hóa.");
        }

        return "redirect:/admin/coupons";
    }
}

