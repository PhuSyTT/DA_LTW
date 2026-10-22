package com.bookstore.controller;

import com.bookstore.controller.admin.PosController;
import com.bookstore.dto.PosCheckoutRequestDto;
import com.bookstore.dto.PosReceiptDto;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosControllerTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private MasterOrderRepository masterOrderRepository;

    @Mock
    private SubOrderRepository subOrderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private PosController posController;

    private Branch sampleBranch;
    private Book sampleBook;
    private BookItem sampleBookItem;
    private User sampleCashier;

    @BeforeEach
    void setUp() {
        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "120 Nguyễn Huệ", "028.3822.1101", "Hồ Chí Minh", null, null, true);
        sampleBook = new Book(1L, "Tuổi Thơ Dữ Dội", "978-604-2-08888-5", null, null, "Kim Đồng", 2020, new BigDecimal("120000"), "/img/cover.jpg", "Mô tả");
        
        sampleBookItem = new BookItem();
        sampleBookItem.setId(10L);
        sampleBookItem.setBook(sampleBook);
        sampleBookItem.setBranch(sampleBranch);
        sampleBookItem.setSkuBarcode("Q1-TTDD-90-001");
        sampleBookItem.setConditionGrade("LIKE_NEW");
        sampleBookItem.setSellingPrice(new BigDecimal("85000"));
        sampleBookItem.setStockQuantity(1);
        sampleBookItem.setStatus("AVAILABLE");

        Role roleCashier = new Role(4L, "CASHIER", "Thu ngân quầy");
        sampleCashier = new User(4L, "cashier.book4life@gmail.com", "1234@abc", "Phạm Thu Ngân", "0904567890", null, roleCashier, sampleBranch, true, null);
    }

    @Test
    @DisplayName("POS UI - Hiển thị màn hình bán hàng với chi nhánh đã gán cho Thu ngân")
    void testShowPosScreen() {
        CustomUserDetails userDetails = new CustomUserDetails(sampleCashier);
        when(branchRepository.findAll()).thenReturn(List.of(sampleBranch));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));

        Model model = new ConcurrentModel();
        String viewName = posController.showPosScreen(userDetails, null, model);

        assertEquals("admin/pos/index", viewName);
        assertEquals(sampleBranch, model.getAttribute("currentBranch"));
        assertEquals("pos", model.getAttribute("activeMenu"));
    }

    @Test
    @DisplayName("POS Search - Tìm kiếm sách sẵn có tại chi nhánh")
    void testSearchPosItems() {
        when(bookItemRepository.searchBookItems(eq("Tuổi Thơ"), eq(1L), isNull(), eq("AVAILABLE"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBookItem)));

        ResponseEntity<?> response = posController.searchPosItems(1L, "Tuổi Thơ");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<?> list = (List<?>) response.getBody();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("POS Checkout - Thanh toán thành công tại quầy và trừ tồn kho")
    void testProcessPosCheckout_Success() {
        CustomUserDetails userDetails = new CustomUserDetails(sampleCashier);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));
        when(bookItemRepository.findByIdWithLock(10L)).thenReturn(Optional.of(sampleBookItem));
        when(masterOrderRepository.save(any(MasterOrder.class))).thenAnswer(i -> i.getArgument(0));

        PosCheckoutRequestDto request = new PosCheckoutRequestDto();
        request.setBranchId(1L);
        request.setBookItemIds(List.of(10L));
        request.setCustomerName("Khách Mua Trực Tiếp");
        request.setCustomerPhone("0988776655");
        request.setPaymentMethod("CASH");
        request.setAmountGiven(new BigDecimal("100000"));

        ResponseEntity<?> response = posController.processPosCheckout(request, userDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        PosReceiptDto receipt = (PosReceiptDto) response.getBody();
        assertNotNull(receipt);
        assertEquals(new BigDecimal("85000"), receipt.getSubtotal());
        assertEquals(new BigDecimal("85000"), receipt.getFinalTotal());
        assertEquals(new BigDecimal("15000"), receipt.getChangeAmount());
        assertEquals("SOLD", sampleBookItem.getStatus());
        assertEquals(0, sampleBookItem.getStockQuantity());

        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("POS Checkout - Giỏ hàng rỗng báo lỗi Bad Request")
    void testProcessPosCheckout_EmptyCart() {
        PosCheckoutRequestDto request = new PosCheckoutRequestDto();
        request.setBranchId(1L);
        request.setBookItemIds(Collections.emptyList());

        ResponseEntity<?> response = posController.processPosCheckout(request, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

