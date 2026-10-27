package com.bookstore.service.impl;

import com.bookstore.dto.ReviewDto;
import com.bookstore.dto.ReviewSummaryDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.Review;
import com.bookstore.entity.SubOrder;
import com.bookstore.entity.SubOrderItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.repository.SubOrderRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SubOrderRepository subOrderRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             BookRepository bookRepository,
                             UserRepository userRepository,
                             SubOrderRepository subOrderRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.subOrderRepository = subOrderRepository;
    }

    @Override
    public ReviewDto createReview(Long userId, Long bookId, Long orderId, Integer rating, String title, String comment) {
        if (userId == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để thực hiện đánh giá sản phẩm!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản người dùng!"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã ID: " + bookId));

        // Kiểm tra quyền mua hàng của khách hàng
        boolean isPurchased = hasPurchasedBook(userId, bookId);
        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        if (!isPurchased && !isAdmin) {
            throw new IllegalStateException("Bạn chỉ có quyền đánh giá sản phẩm sau khi đã mua và nhận sách thành công!");
        }

        // Mặc định rating là 5* nếu không truyền hoặc không hợp lệ
        int finalRating = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;

        String subOrderCode = null;
        if (orderId != null) {
            SubOrder subOrder = subOrderRepository.findById(orderId).orElse(null);
            if (subOrder != null) {
                subOrderCode = subOrder.getSubOrderCode();
            }
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setOrderId(orderId);
        review.setSubOrderCode(subOrderCode);
        review.setRating(finalRating);
        review.setTitle((title != null && !title.isBlank()) ? title.trim() : ("Đánh giá " + finalRating + " sao"));
        review.setComment((comment != null) ? comment.trim() : "");
        review.setIsVerifiedPurchase(isPurchased);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummaryForBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã: " + bookId));

        ReviewSummaryDto summary = new ReviewSummaryDto();
        summary.setBookId(book.getId());
        summary.setBookTitle(book.getTitle());
        if (book.getAuthor() != null) {
            summary.setAuthorName(book.getAuthor().getName());
        }
        summary.setCoverImageUrl(book.getCoverImageUrl());

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        long total = reviews.size();
        summary.setTotalReviews(total);

        if (total == 0) {
            summary.setAverageRating(5.0); // Mặc định hiển thị chuẩn 5 sao khi sách mới
            summary.setStar5Percent(100);
            summary.setReviews(new ArrayList<>());
            return summary;
        }

        long count5 = 0, count4 = 0, count3 = 0, count2 = 0, count1 = 0;
        double sum = 0;

        for (Review r : reviews) {
            int stars = r.getRating() != null ? r.getRating() : 5;
            sum += stars;
            switch (stars) {
                case 5 -> count5++;
                case 4 -> count4++;
                case 3 -> count3++;
                case 2 -> count2++;
                case 1 -> count1++;
                default -> count5++;
            }
        }

        double avg = Math.round((sum / total) * 10.0) / 10.0;
        summary.setAverageRating(avg);

        summary.setStar5Count(count5);
        summary.setStar4Count(count4);
        summary.setStar3Count(count3);
        summary.setStar2Count(count2);
        summary.setStar1Count(count1);

        summary.setStar5Percent((int) Math.round((count5 * 100.0) / total));
        summary.setStar4Percent((int) Math.round((count4 * 100.0) / total));
        summary.setStar3Percent((int) Math.round((count3 * 100.0) / total));
        summary.setStar2Percent((int) Math.round((count2 * 100.0) / total));
        summary.setStar1Percent((int) Math.round((count1 * 100.0) / total));

        summary.setReviews(reviews.stream().map(this::mapToDto).collect(Collectors.toList()));
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByBook(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPurchasedBook(Long userId, Long bookId) {
        if (userId == null || bookId == null) return false;

        // Tìm tất cả các SubOrder của người dùng
        List<SubOrder> userSubOrders = subOrderRepository.findAll();
        for (SubOrder so : userSubOrders) {
            if (so.getMasterOrder() != null && so.getMasterOrder().getUser() != null
                    && userId.equals(so.getMasterOrder().getUser().getId())) {
                // Kiểm tra trạng thái đơn: COMPLETED hoặc DELIVERED hoặc SHIPPED
                String status = so.getStatus();
                if ("COMPLETED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status) || "SHIPPED".equalsIgnoreCase(status)) {
                    if (so.getItems() != null) {
                        for (SubOrderItem item : so.getItems()) {
                            if (item.getBookItem() != null && item.getBookItem().getBook() != null
                                    && bookId.equals(item.getBookItem().getBook().getId())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewBook(Long userId, Long bookId, Long orderId) {
        if (userId == null || bookId == null) return false;
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            return true;
        }
        return hasPurchasedBook(userId, bookId);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (reviewId != null && reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
        }
    }

    private ReviewDto mapToDto(Review entity) {
        ReviewDto dto = new ReviewDto();
        dto.setId(entity.getId());
        if (entity.getBook() != null) {
            dto.setBookId(entity.getBook().getId());
            dto.setBookTitle(entity.getBook().getTitle());
            dto.setBookCoverImage(entity.getBook().getCoverImageUrl());
            if (entity.getBook().getAuthor() != null) {
                dto.setAuthorName(entity.getBook().getAuthor().getName());
            }
        }

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getFullName() != null ? entity.getUser().getFullName() : "Độc giả Book4Life");
            dto.setUserEmail(entity.getUser().getEmail());
        }

        dto.setOrderId(entity.getOrderId());
        dto.setSubOrderCode(entity.getSubOrderCode());
        dto.setRating(entity.getRating() != null ? entity.getRating() : 5);
        dto.setTitle(entity.getTitle());
        dto.setComment(entity.getComment());
        dto.setIsVerifiedPurchase(entity.getIsVerifiedPurchase());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}
