package com.bookstore.service;

import com.bookstore.dto.ReviewDto;
import com.bookstore.dto.ReviewSummaryDto;

import java.util.List;

public interface ReviewService {

    /**
     * Tạo và lưu đánh giá sản phẩm mới
     */
    ReviewDto createReview(Long userId, Long bookId, Long orderId, Integer rating, String title, String comment);

    /**
     * Lấy bảng tổng hợp đánh giá (Overall review summary) cho một cuốn sách
     */
    ReviewSummaryDto getReviewSummaryForBook(Long bookId);

    /**
     * Lấy toàn bộ đánh giá trong hệ thống (phục vụ trang quản trị / giám sát phản hồi)
     */
    List<ReviewDto> getAllReviews();

    /**
     * Lấy danh sách đánh giá theo sách
     */
    List<ReviewDto> getReviewsByBook(Long bookId);

    /**
     * Kiểm tra xem khách hàng đã mua sản phẩm này và nhận hàng thành công hay chưa
     */
    boolean hasPurchasedBook(Long userId, Long bookId);

    /**
     * Kiểm tra xem khách hàng có quyền đánh giá sản phẩm hay không
     */
    boolean canUserReviewBook(Long userId, Long bookId, Long orderId);

    /**
     * Xóa đánh giá (chỉ ADMIN)
     */
    void deleteReview(Long reviewId);
}
