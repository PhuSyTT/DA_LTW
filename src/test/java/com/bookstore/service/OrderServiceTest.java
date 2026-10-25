package com.bookstore.service;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private MasterOrderRepository masterOrderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User sampleUser;
    private Branch branchQ1;
    private Book book;
    private BookItem bookItem;

    @BeforeEach
    void setUp() {
        Role role = new Role(1L, "ROLE_CUSTOMER", "Khách hàng");
        sampleUser = new User(100L, "customer@test.com", "pass", "Nguyễn Văn Test", "0901234567", null, role, null, true, LocalDateTime.now());

        branchQ1 = new Branch(1L, "Book4Life Quận 1", "123 Lê Lợi, Q1", "0901234567", "Hồ Chí Minh", null, null, true);
        book = new Book(10L, "Nhà Giả Kim", "978-604-1-12345-6", null, null, "NXB Trẻ", 2020, new BigDecimal("100000"), null, "Mô tả");
        bookItem = new BookItem(1L, book, branchQ1, "SKU-001", "LIKE_NEW", new BigDecimal("75000"), 5, "Kệ A1", "Sách tốt", LocalDateTime.now(), "AVAILABLE");
    }

    @Test
    @DisplayName("Đặt hàng thành công với COD: dữ liệu và số tiền được đồng bộ chính xác")
    void testProcessCheckout_COD_Success() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));
        when(masterOrderRepository.save(any(MasterOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setReceiverName("Nguyễn Văn Test");
        req.setReceiverPhone("0901234567");
        req.setShippingAddress("123 Lê Lợi, Q1");
        req.setPaymentMethod("COD");
        req.setBookItemIds(List.of(1L));
        req.setItemQuantities(Map.of(1L, 2));
        req.setTotalItemsAmount(new BigDecimal("150000"));
        req.setShippingFee(new BigDecimal("15000"));
        req.setDiscountAmount(new BigDecimal("10000"));
        req.setShippingDiscountAmount(BigDecimal.ZERO);
        req.setFinalAmount(new BigDecimal("155000"));

        MasterOrder master = orderService.processCheckout(req, sampleUser);

        assertNotNull(master);
        assertEquals("COD", master.getPaymentMethod());
        assertEquals("PENDING", master.getPaymentStatus());
        assertEquals(new BigDecimal("150000"), master.getTotalItemsAmount());
        assertEquals(new BigDecimal("15000"), master.getTotalShippingFee());
        assertEquals(new BigDecimal("10000"), master.getDiscountAmount());
        assertEquals(new BigDecimal("155000"), master.getFinalAmount());
        assertEquals(1, master.getSubOrders().size());

        SubOrder sub = master.getSubOrders().get(0);
        assertEquals(branchQ1, sub.getBranch());
        assertEquals(1, sub.getItems().size());
        assertEquals(2, sub.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("150000"), sub.getBranchSubtotal());

        // Kiểm tra tồn kho giảm đúng 2 (từ 5 còn 3) và vẫn giữ AVAILABLE
        assertEquals(3, bookItem.getStockQuantity());
        assertEquals("AVAILABLE", bookItem.getStatus());
        verify(bookItemRepository, times(1)).save(bookItem);
        verify(masterOrderRepository, times(1)).save(any(MasterOrder.class));
    }

    @Test
    @DisplayName("Đặt hàng thành công với POS khi mua trực tiếp / giữ sách tại quầy")
    void testProcessCheckout_POS_ClickAndCollect() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));
        when(masterOrderRepository.save(any(MasterOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setReceiverName("Nguyễn Văn Test");
        req.setReceiverPhone("0901234567");
        req.setShippingAddress("Nhận tại quầy Book4Life Q1");
        req.setPaymentMethod("POS");
        req.setClickAndCollect(true);
        req.setBookItemIds(List.of(1L));
        req.setItemQuantities(Map.of(1L, 1));
        req.setTotalItemsAmount(new BigDecimal("75000"));
        req.setShippingFee(BigDecimal.ZERO);
        req.setFinalAmount(new BigDecimal("75000"));

        MasterOrder master = orderService.processCheckout(req, sampleUser);

        assertNotNull(master);
        assertEquals("POS", master.getPaymentMethod());
        assertEquals("PENDING", master.getPaymentStatus());
        assertEquals(BigDecimal.ZERO, master.getTotalShippingFee());
        assertEquals(new BigDecimal("75000"), master.getFinalAmount());
    }

    @Test
    @DisplayName("Đặt hàng khi số lượng tồn kho giảm về 0: trạng thái chuyển sang SOLD")
    void testProcessCheckout_StockZero_StatusSold() {
        bookItem.setStockQuantity(1);
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));
        when(masterOrderRepository.save(any(MasterOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setReceiverName("Nguyễn Văn Test");
        req.setReceiverPhone("0901234567");
        req.setShippingAddress("123 Lê Lợi, Q1");
        req.setPaymentMethod("COD");
        req.setBookItemIds(List.of(1L));
        req.setItemQuantities(Map.of(1L, 1));

        MasterOrder master = orderService.processCheckout(req, sampleUser);

        assertNotNull(master);
        assertEquals(0, bookItem.getStockQuantity());
        assertEquals("SOLD", bookItem.getStatus());
    }

    @Test
    @DisplayName("Đặt hàng khi sản phẩm đã hết hàng: quăng ngoại lệ rõ ràng")
    void testProcessCheckout_OutOfStock_ThrowsException() {
        bookItem.setStockQuantity(0);
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setBookItemIds(List.of(1L));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.processCheckout(req, sampleUser));
        assertTrue(ex.getMessage().contains("hết hàng"));
    }

    @Test
    @DisplayName("Xử lý tranh chấp 2 người cùng đặt 1 cuốn sách: Người thứ 2 bị chặn vì sách đã SOLD hoặc hết kho")
    void testProcessCheckout_ConcurrentConflict_SecondUserBlocked() {
        // Mô phỏng User 1 vừa đặt xong cuốn sách duy nhất (stock = 0, status = SOLD)
        bookItem.setStockQuantity(0);
        bookItem.setStatus("SOLD");
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setBookItemIds(List.of(1L));
        req.setItemQuantities(Map.of(1L, 1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.processCheckout(req, sampleUser));
        assertTrue(ex.getMessage().contains("vừa có khách hàng khác đặt mua") || ex.getMessage().contains("hết hàng"));
    }

    @Test
    @DisplayName("Đặt số lượng vượt quá tồn kho: báo lỗi không đủ số lượng")
    void testProcessCheckout_InsufficientStock_ThrowsException() {
        bookItem.setStockQuantity(1);
        when(bookItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(bookItem));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setBookItemIds(List.of(1L));
        req.setItemQuantities(Map.of(1L, 3)); // Đặt 3 cuốn trong khi kho chỉ có 1

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.processCheckout(req, sampleUser));
        assertTrue(ex.getMessage().contains("không đủ số lượng"));
    }
}

