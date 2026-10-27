package com.bookstore.service;

import com.bookstore.dto.ReviewDto;
import com.bookstore.dto.ReviewSummaryDto;
import com.bookstore.entity.*;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.repository.SubOrderRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubOrderRepository subOrderRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User sampleUser;
    private User sampleAdmin;
    private Book sampleBook;
    private SubOrder sampleCompletedSubOrder;

    @BeforeEach
    void setUp() {
        Role roleCustomer = new Role();
        roleCustomer.setId(5L);
        roleCustomer.setRoleName("CUSTOMER");

        Role roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setRoleName("ADMIN");

        sampleUser = new User();
        sampleUser.setId(5L);
        sampleUser.setFullName("Hoàng Độc Giả");
        sampleUser.setEmail("user.book4life@gmail.com");
        sampleUser.setRole(roleCustomer);

        sampleAdmin = new User();
        sampleAdmin.setId(1L);
        sampleAdmin.setFullName("Nguyễn Quản Trị");
        sampleAdmin.setEmail("admin.book4life@gmail.com");
        sampleAdmin.setRole(roleAdmin);

        Author author = new Author();
        author.setId(1L);
        author.setName("Nam Cao");

        sampleBook = new Book();
        sampleBook.setId(10L);
        sampleBook.setTitle("Chí Phèo");
        sampleBook.setAuthor(author);

        // SubOrder completed setup
        MasterOrder masterOrder = new MasterOrder();
        masterOrder.setId(100L);
        masterOrder.setUser(sampleUser);

        sampleCompletedSubOrder = new SubOrder();
        sampleCompletedSubOrder.setId(200L);
        sampleCompletedSubOrder.setSubOrderCode("SUB-100-1");
        sampleCompletedSubOrder.setStatus("COMPLETED");
        sampleCompletedSubOrder.setMasterOrder(masterOrder);

        BookItem bookItem = new BookItem();
        bookItem.setId(300L);
        bookItem.setBook(sampleBook);

        SubOrderItem subOrderItem = new SubOrderItem();
        subOrderItem.setId(400L);
        subOrderItem.setSubOrder(sampleCompletedSubOrder);
        subOrderItem.setBookItem(bookItem);

        sampleCompletedSubOrder.setItems(List.of(subOrderItem));
    }

    @Test
    @DisplayName("Khách hàng đã mua sách có quyền đánh giá và mặc định là 5 sao khi không chọn")
    void testCreateReview_Default5StarWhenEmpty() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(subOrderRepository.findAll()).thenReturn(List.of(sampleCompletedSubOrder));
        when(subOrderRepository.findById(200L)).thenReturn(Optional.of(sampleCompletedSubOrder));

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        // rating = null -> mặc định 5 sao
        ReviewDto result = reviewService.createReview(5L, 10L, 200L, null, "Sách rất đẹp", "Đóng gói bọc cẩn thận");

        assertNotNull(result);
        assertEquals(5, result.getRating(), "Mặc định đánh giá phải là 5 sao");
        assertTrue(result.getIsVerifiedPurchase(), "Phải đánh dấu là đã mua hàng xác thực");
        assertEquals("Sách rất đẹp", result.getTitle());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Khách hàng chưa mua sách hoặc đơn chưa hoàn tất không có quyền đánh giá")
    void testCreateReview_ThrowsWhenNotPurchased() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        // Không có suborder completed nào cho sách này
        when(subOrderRepository.findAll()).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(5L, 10L, null, 5, "Tiêu đề", "Nhận xét");
        });

        assertTrue(ex.getMessage().contains("chỉ có quyền đánh giá sản phẩm sau khi đã mua"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Tài khoản ADMIN có thể viết đánh giá mẫu thẩm định")
    void testCreateReview_AdminAllowed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(subOrderRepository.findAll()).thenReturn(List.of());

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        ReviewDto result = reviewService.createReview(1L, 10L, null, 5, "Thẩm định chất lượng", "Sách chuẩn Like New");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Kiểm tra tính điểm trung bình và phân bố % sao trên trang đánh giá tổng")
    void testGetReviewSummaryForBook() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        Review r1 = new Review();
        r1.setId(1L);
        r1.setBook(sampleBook);
        r1.setUser(sampleUser);
        r1.setRating(5);
        r1.setCreatedAt(LocalDateTime.now());

        Review r2 = new Review();
        r2.setId(2L);
        r2.setBook(sampleBook);
        r2.setUser(sampleUser);
        r2.setRating(5);
        r2.setCreatedAt(LocalDateTime.now());

        Review r3 = new Review();
        r3.setId(3L);
        r3.setBook(sampleBook);
        r3.setUser(sampleUser);
        r3.setRating(4);
        r3.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r1, r2, r3));

        ReviewSummaryDto summary = reviewService.getReviewSummaryForBook(10L);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalReviews());
        assertEquals(2, summary.getStar5Count());
        assertEquals(1, summary.getStar4Count());
        // Điểm TB: (5 + 5 + 4) / 3 = 14 / 3 = 4.7
        assertEquals(4.7, summary.getAverageRating());
        assertEquals(67, summary.getStar5Percent()); // 2/3 = 67%
        assertEquals(33, summary.getStar4Percent()); // 1/3 = 33%
    }

    @Test
    @DisplayName("hasPurchasedBook trả về false nếu đơn hàng đang ở trạng thái PENDING")
    void testHasPurchasedBook_PendingReturnsFalse() {
        sampleCompletedSubOrder.setStatus("PENDING");
        when(subOrderRepository.findAll()).thenReturn(List.of(sampleCompletedSubOrder));

        boolean purchased = reviewService.hasPurchasedBook(5L, 10L);
        assertFalse(purchased, "Đơn hàng PENDING chưa được tính là đã nhận hàng thành công");
    }
}
