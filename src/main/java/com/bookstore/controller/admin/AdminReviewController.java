package com.bookstore.controller.admin;

import com.bookstore.dto.ReviewDto;
import com.bookstore.service.ReviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN', 'ROLE_BRANCH_MANAGER', 'BRANCH_MANAGER')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Trang xem đánh giá tổng toàn chuỗi dành cho Admin & Branch Managers
     */
    @GetMapping
    public String listAllReviews(
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        List<ReviewDto> reviews = reviewService.getAllReviews();

        // Lọc theo rating nếu có
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviews.stream()
                    .filter(r -> r.getRating() != null && r.getRating().equals(rating))
                    .toList();
        }

        // Lọc theo từ khóa tìm kiếm (tên sách hoặc nội dung)
        if (keyword != null && !keyword.isBlank()) {
            String kwLower = keyword.trim().toLowerCase();
            reviews = reviews.stream()
                    .filter(r -> (r.getBookTitle() != null && r.getBookTitle().toLowerCase().contains(kwLower))
                            || (r.getComment() != null && r.getComment().toLowerCase().contains(kwLower))
                            || (r.getUserName() != null && r.getUserName().toLowerCase().contains(kwLower)))
                    .toList();
        }

        // Thống kê sơ bộ
        long total = reviews.size();
        double avg = reviews.stream().mapToInt(ReviewDto::getRating).average().orElse(5.0);

        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", total);
        model.addAttribute("avgRating", Math.round(avg * 10.0) / 10.0);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "reviews");
        model.addAttribute("pageTitle", "Quản Lý Đánh Giá & Phản Hồi Độc Giả");
        model.addAttribute("pageSubtitle", "Theo dõi mức độ hài lòng, chất lượng sách thực tế và phản hồi từ khách hàng");

        return "admin/reviews/list";
    }

    /**
     * Xóa đánh giá không phù hợp (Chỉ ADMIN)
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public String deleteReview(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đánh giá: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}
