package com.bookstore.service;

import com.bookstore.dto.WishlistRequestDto;
import com.bookstore.dto.WishlistResponseDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.entity.Wishlist;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User sampleUser;
    private Book sampleBook;
    private Branch sampleBranch;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role(1L, "CUSTOMER", "Khách hàng");
        sampleUser = new User(1L, "customer@example.com", "pass", "Nguyen Van A", "0900000001", null, customerRole, null, true, null);

        sampleBook = Book.builder()
                .id(1L)
                .title("Nhà Giả Kim")
                .isbn("9786045622452")
                .originalPrice(new BigDecimal("79000"))
                .build();

        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "123 Lê Lợi", "0901234567", "Hồ Chí Minh", null, null, true);
    }

    @Test
    @DisplayName("Khách hàng đăng ký săn sách khi hết hàng (FR-WSH-01)")
    void testAddToWishlist() {
        WishlistRequestDto request = new WishlistRequestDto(1L, 1L, "VERY_GOOD");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));
        when(wishlistRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist w = invocation.getArgument(0);
            w.setId(10L);
            return w;
        });

        WishlistResponseDto response = wishlistService.addToWishlist(request, sampleUser);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Nhà Giả Kim", response.getBookTitle());
        assertEquals("VERY_GOOD", response.getMinConditionGrade());
        assertFalse(response.getIsNotified());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("Tự động kích hoạt thông báo khi sách về kho thỏa mãn điều kiện (FR-WSH-02)")
    void testCheckAndTriggerRestockAlerts_Success() {
        Wishlist pendingWishlist = new Wishlist(sampleUser, sampleBook, sampleBranch, "GOOD");
        pendingWishlist.setId(5L);

        when(wishlistRepository.findByBookIdAndIsNotifiedFalse(1L)).thenReturn(List.of(pendingWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Nhập kho sách độ cũ LIKE_NEW (cao hơn yêu cầu GOOD)
        BookItem newItem = new BookItem(20L, sampleBook, sampleBranch, "Q1-NGK-001", "LIKE_NEW",
                new BigDecimal("65000"), 1, "Kệ Văn Học", "Mới tinh", null, "AVAILABLE");

        int count = wishlistService.checkAndTriggerRestockAlerts(newItem);

        assertEquals(1, count);
        assertTrue(pendingWishlist.getIsNotified());
        assertNotNull(pendingWishlist.getNotifiedAt());
        verify(wishlistRepository, times(1)).save(pendingWishlist);
    }

    @Test
    @DisplayName("Không bắn thông báo nếu độ cũ thực tế thấp hơn kỳ vọng tối thiểu của khách")
    void testCheckAndTriggerRestockAlerts_ConditionGradeTooLow() {
        // Khách yêu cầu tối thiểu LIKE_NEW
        Wishlist pickyWishlist = new Wishlist(sampleUser, sampleBook, sampleBranch, "LIKE_NEW");
        pickyWishlist.setId(6L);

        when(wishlistRepository.findByBookIdAndIsNotifiedFalse(1L)).thenReturn(List.of(pickyWishlist));

        // Sách nhập kho chỉ là ACCEPTABLE (không đủ tiêu chuẩn LIKE_NEW)
        BookItem lowGradeItem = new BookItem(21L, sampleBook, sampleBranch, "Q1-NGK-002", "ACCEPTABLE",
                new BigDecimal("30000"), 1, "Kệ Văn Học", "Cũ nhiều", null, "AVAILABLE");

        int count = wishlistService.checkAndTriggerRestockAlerts(lowGradeItem);

        assertEquals(0, count);
        assertFalse(pickyWishlist.getIsNotified());
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }
}
