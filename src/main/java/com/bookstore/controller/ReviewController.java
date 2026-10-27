package com.bookstore.controller;

import com.bookstore.dto.ReviewDto;
import com.bookstore.dto.ReviewSummaryDto;
import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final BookRepository bookRepository;

    public ReviewController(ReviewService reviewService, BookRepository bookRepository) {
        this.reviewService = reviewService;
        this.bookRepository = bookRepository;
    }

    /**
     * Trang xem đánh giá tổng quan của một đầu sách (Public)
     */
    @GetMapping("/books/{bookId}/reviews")
    public String viewBookReviews(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã: " + bookId));

        ReviewSummaryDto summary = reviewService.getReviewSummaryForBook(bookId);

        boolean canReview = false;
        if (userDetails != null) {
            canReview = reviewService.canUserReviewBook(userDetails.getId(), bookId, null);
        }

        model.addAttribute("book", book);
        model.addAttribute("summary", summary);
        model.addAttribute("canReview", canReview);
        model.addAttribute("pageTitle", "Đánh Giá & Nhận Xét: " + book.getTitle() + " - Book4Life");

        return "reviews/book-reviews";
    }

    /**
     * Trang xem đánh giá tổng của toàn hệ thống (Public)
     */
    @GetMapping("/reviews")
    public String viewAllReviews(Model model) {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        model.addAttribute("pageTitle", "Góc Độc Giả: Đánh Giá & Phản Hồi Toàn Hệ Thống - Book4Life");
        return "reviews/all-reviews";
    }

    /**
     * Trang form đánh giá sản phẩm (Dành cho khách hàng sau khi mua sản phẩm, mặc định 5*)
     */
    @GetMapping("/books/{bookId}/review")
    public String showReviewForm(
            @PathVariable Long bookId,
            @RequestParam(name = "orderId", required = false) Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login?redirect=/books/" + bookId + "/review" + (orderId != null ? "?orderId=" + orderId : "");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã: " + bookId));

        boolean canReview = reviewService.canUserReviewBook(userDetails.getId(), bookId, orderId);
        if (!canReview) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Bạn chỉ có quyền đánh giá sản phẩm sau khi đã mua và nhận hàng thành công!");
            return "redirect:/books/" + bookId;
        }

        model.addAttribute("book", book);
        model.addAttribute("orderId", orderId);
        model.addAttribute("defaultRating", 5); // Yêu cầu: Mặc định sản phẩm là 5*
        model.addAttribute("pageTitle", "Đánh Giá Sản Phẩm: " + book.getTitle());

        return "reviews/write";
    }

    /**
     * Lưu đánh giá sản phẩm của khách hàng
     */
    @PostMapping("/books/{bookId}/review")
    public String submitReview(
            @PathVariable Long bookId,
            @RequestParam(name = "orderId", required = false) Long orderId,
            @RequestParam(name = "rating", required = false, defaultValue = "5") Integer rating,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "comment", required = false) String comment,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            reviewService.createReview(userDetails.getId(), bookId, orderId, rating, title, comment);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cảm ơn bạn đã gửi đánh giá " + rating + " sao ⭐ cho cuốn sách! Nhận xét của bạn giúp cộng đồng độc giả tự tin chọn sách hơn.");
            return "redirect:/books/" + bookId + "/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi đánh giá: " + e.getMessage());
            return "redirect:/books/" + bookId + "/review" + (orderId != null ? "?orderId=" + orderId : "");
        }
    }
}
