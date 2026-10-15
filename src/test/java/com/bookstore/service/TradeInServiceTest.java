package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.impl.TradeInServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho Nhóm 8: Thu mua sách cũ từ người dùng (Trade-in / Buy-back Workflow)
 * Covers: FR-TRD-01, FR-TRD-03, FR-TRD-04, FR-TRD-05, FR-TRD-06
 */
@ExtendWith(MockitoExtension.class)
class TradeInServiceTest {

    @Mock
    private TradeInRequestRepository tradeInRequestRepository;

    @Mock
    private TradeInImageRepository tradeInImageRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private BookItemImageRepository bookItemImageRepository;

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private TradeInServiceImpl tradeInService;

    private User sampleCustomer;
    private User sampleStaff;
    private Book sampleBook;
    private Branch sampleBranch;
    private TradeInRequest sampleRequest;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role(1L, "CUSTOMER", "Khách hàng");
        Role staffRole = new Role(2L, "STAFF_APPRAISER", "Nhân viên kiểm duyệt");

        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "123 Lê Lợi", "0901234567", "Hồ Chí Minh", null, null, true);

        sampleCustomer = new User(1L, "customer@example.com", "pass", "Nguyễn Văn A", "0900000001", null, customerRole, null, true, null);
        sampleStaff = new User(2L, "appraiser@book4life.vn", "pass", "Trần Thị B", "0900000002", null, staffRole, sampleBranch, true, null);

        sampleBook = Book.builder()
                .id(1L)
                .title("Nhà Giả Kim")
                .isbn("9786045622452")
                .originalPrice(new BigDecimal("79000"))
                .build();

        sampleRequest = new TradeInRequest();
        sampleRequest.setId(10L);
        sampleRequest.setRequestCode("TI-2026-12345");
        sampleRequest.setUser(sampleCustomer);
        sampleRequest.setBook(sampleBook);
        sampleRequest.setTargetBranch(sampleBranch);
        sampleRequest.setUserConditionGrade("GOOD");
        sampleRequest.setSurveyAnswersJson("{\"coverDamaged\":false,\"spineDamaged\":false}");
        sampleRequest.setAutoEstimatedPrice(new BigDecimal("19000"));
        sampleRequest.setStatus("PENDING_REVIEW");
        sampleRequest.setDeliveryMethod("BRING_TO_STORE");
        sampleRequest.setImages(new ArrayList<>());
    }

    // ==============================================================
    // FR-TRD-03: Tự động tính giá định giá ban đầu (Auto Estimate)
    // ==============================================================

    @Test
    @DisplayName("Tính giá ước tính sách LIKE_NEW không có khuyết điểm (FR-TRD-03)")
    void testCalculateAutoEstimate_LikeNew_NoPenalty() {
        AutoEstimateRequestDto request = new AutoEstimateRequestDto();
        request.setBookId(1L);
        request.setUserConditionGrade("LIKE_NEW");
        request.setCoverDamaged(false);
        request.setSpineDamaged(false);
        request.setYellowPages(false);
        request.setMarkingsPresent(false);
        request.setMissingAccessory(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        AutoEstimateResponseDto result = tradeInService.calculateAutoEstimate(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("79000"), result.getOriginalPrice());
        assertEquals(new BigDecimal("0.45"), result.getBaseRate()); // LIKE_NEW = 45%
        assertEquals(BigDecimal.ZERO, result.getTotalPenalty());
        assertTrue(result.getEstimatedPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Tính giá ước tính sách GOOD với khuyết điểm bìa+gáy (FR-TRD-03)")
    void testCalculateAutoEstimate_Good_WithPenalties() {
        AutoEstimateRequestDto request = new AutoEstimateRequestDto();
        request.setBookId(1L);
        request.setUserConditionGrade("GOOD");
        request.setCoverDamaged(true);    // -5%
        request.setSpineDamaged(true);    // -10%
        request.setYellowPages(false);
        request.setMarkingsPresent(false);
        request.setMissingAccessory(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        AutoEstimateResponseDto result = tradeInService.calculateAutoEstimate(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.25"), result.getBaseRate()); // GOOD = 25%
        assertEquals(new BigDecimal("0.15"), result.getTotalPenalty()); // 5% + 10%
        assertTrue(result.getPenaltyBreakdown().containsKey("coverDamaged"));
        assertTrue(result.getPenaltyBreakdown().containsKey("spineDamaged"));
    }

    @Test
    @DisplayName("Tính giá ước tính sách ACCEPTABLE với đầy đủ khuyết điểm (FR-TRD-03)")
    void testCalculateAutoEstimate_Acceptable_AllPenalties() {
        AutoEstimateRequestDto request = new AutoEstimateRequestDto();
        request.setBookId(1L);
        request.setUserConditionGrade("ACCEPTABLE");
        request.setCoverDamaged(true);      // -5%
        request.setSpineDamaged(true);      // -10%
        request.setYellowPages(true);       // -10%
        request.setMarkingsPresent(true);   // -10%
        request.setMissingAccessory(true);  // -5%

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        AutoEstimateResponseDto result = tradeInService.calculateAutoEstimate(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.40"), result.getTotalPenalty()); // 5+10+10+10+5 = 40%
        assertEquals(5, result.getPenaltyBreakdown().size());
        assertTrue(result.getEstimatedPrice().compareTo(BigDecimal.ZERO) >= 0);
    }

    // ==============================================================
    // FR-TRD-01 + FR-TRD-02: Tạo phiếu thu mua kèm ảnh
    // ==============================================================

    @Test
    @DisplayName("Khách hàng gửi phiếu bán sách cũ thành công - Status PENDING_REVIEW (FR-TRD-01 & FR-TRD-02)")
    void testCreateTradeInRequest_Success() {
        TradeInCreateDto dto = new TradeInCreateDto();
        dto.setBookId(1L);
        dto.setTargetBranchId(1L);
        dto.setUserConditionGrade("GOOD");
        dto.setCoverDamaged(false);
        dto.setSpineDamaged(false);
        dto.setYellowPages(false);
        dto.setMarkingsPresent(false);
        dto.setMissingAccessory(false);
        dto.setUserNote("Sách còn khá mới, ít đọc");
        dto.setDeliveryMethod("BRING_TO_STORE");

        TradeInImageDto img1 = new TradeInImageDto("https://res.cloudinary.com/abc/cover.jpg", "abc/cover", "COVER");
        TradeInImageDto img2 = new TradeInImageDto("https://res.cloudinary.com/abc/spine.jpg", "abc/spine", "SPINE");
        TradeInImageDto img3 = new TradeInImageDto("https://res.cloudinary.com/abc/page.jpg", "abc/page", "INNER");
        dto.setImages(List.of(img1, img2, img3));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> {
            TradeInRequest r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(tradeInImageRepository.saveAll(any())).thenReturn(List.of());

        TradeInResponseDto response = tradeInService.createTradeInRequest(dto, sampleCustomer);

        assertNotNull(response);
        assertEquals("PENDING_REVIEW", response.getStatus());
        assertNotNull(response.getRequestCode());
        assertTrue(response.getAutoEstimatedPrice().compareTo(BigDecimal.ZERO) > 0);
        verify(tradeInRequestRepository, times(1)).save(any(TradeInRequest.class));
    }

    @Test
    @DisplayName("Không cho tạo phiếu nếu user null (FR-TRD-01)")
    void testCreateTradeInRequest_UserNull_ThrowsException() {
        TradeInCreateDto dto = new TradeInCreateDto();
        dto.setBookId(1L);
        dto.setTargetBranchId(1L);
        dto.setUserConditionGrade("GOOD");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tradeInService.createTradeInRequest(dto, null));
        assertTrue(ex.getMessage().contains("đăng nhập"));
    }

    // ==============================================================
    // FR-TRD-04: Nhân viên kiểm duyệt thẩm định & Định giá lại
    // ==============================================================

    @Test
    @DisplayName("Nhân viên chấp nhận giá ban đầu, trạng thái chuyển sang OFFERED (FR-TRD-04)")
    void testReviewTradeInRequest_AcceptInitialEstimate() {
        TradeInReviewDto reviewDto = new TradeInReviewDto();
        reviewDto.setAcceptInitialEstimate(true);
        reviewDto.setStaffNotes("Ảnh đẹp, đúng như mô tả");

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeInResponseDto result = tradeInService.reviewTradeInRequest(10L, reviewDto, sampleStaff);

        assertNotNull(result);
        assertEquals("OFFERED", result.getStatus());
        assertEquals(sampleRequest.getAutoEstimatedPrice(), result.getStaffReviewedPrice());
    }

    @Test
    @DisplayName("Nhân viên định giá lại thấp hơn, trạng thái chuyển sang OFFERED (FR-TRD-04)")
    void testReviewTradeInRequest_ReviewedPriceLower() {
        TradeInReviewDto reviewDto = new TradeInReviewDto();
        reviewDto.setAcceptInitialEstimate(false);
        reviewDto.setStaffReviewedPrice(new BigDecimal("14000"));
        reviewDto.setStaffConditionGrade("ACCEPTABLE");
        reviewDto.setStaffNotes("Gáy sách có dấu hiệu bung keo, trang ố vàng đậm hơn ảnh");

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeInResponseDto result = tradeInService.reviewTradeInRequest(10L, reviewDto, sampleStaff);

        assertNotNull(result);
        assertEquals("OFFERED", result.getStatus());
        assertEquals(new BigDecimal("14000"), result.getStaffReviewedPrice());
        assertEquals("ACCEPTABLE", result.getStaffConditionGrade());
    }

    @Test
    @DisplayName("Không thể thẩm định phiếu không ở trạng thái PENDING_REVIEW (FR-TRD-04)")
    void testReviewTradeInRequest_WrongStatus_ThrowsException() {
        sampleRequest.setStatus("OFFERED"); // Đã được báo giá rồi

        TradeInReviewDto reviewDto = new TradeInReviewDto();
        reviewDto.setAcceptInitialEstimate(true);

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tradeInService.reviewTradeInRequest(10L, reviewDto, sampleStaff));
        assertTrue(ex.getMessage().contains("PENDING_REVIEW"));
    }

    // ==============================================================
    // FR-TRD-05: Khách hàng phản hồi kết quả định giá
    // ==============================================================

    @Test
    @DisplayName("Khách hàng đồng ý mức giá - Status chuyển sang ACCEPTED_AWAITING_DELIVERY (FR-TRD-05)")
    void testCustomerRespondToOffer_Accepted() {
        sampleRequest.setStatus("OFFERED");
        sampleRequest.setStaffReviewedPrice(new BigDecimal("17000"));

        TradeInCustomerResponseDto responseDto = new TradeInCustomerResponseDto();
        responseDto.setAccepted(true);
        responseDto.setDeliveryMethod("BRING_TO_STORE");

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeInResponseDto result = tradeInService.customerRespondToOffer(10L, responseDto, sampleCustomer);

        assertNotNull(result);
        assertEquals("ACCEPTED_AWAITING_DELIVERY", result.getStatus());
    }

    @Test
    @DisplayName("Khách hàng từ chối mức giá - Status chuyển sang CANCELLED (FR-TRD-05)")
    void testCustomerRespondToOffer_Rejected() {
        sampleRequest.setStatus("OFFERED");

        TradeInCustomerResponseDto responseDto = new TradeInCustomerResponseDto();
        responseDto.setAccepted(false);

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeInResponseDto result = tradeInService.customerRespondToOffer(10L, responseDto, sampleCustomer);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
    }

    // ==============================================================
    // FR-TRD-06: Nhận sách thực tế & Nhập kho - Tạo phiếu chi
    // ==============================================================

    @Test
    @DisplayName("Nhân viên xác nhận nhập kho thành công - Tạo BookItem mới (FR-TRD-06)")
    void testConfirmPhysicalReceiptAndStock_Success() {
        sampleRequest.setStatus("ACCEPTED_AWAITING_DELIVERY");
        sampleRequest.setStaffReviewedPrice(new BigDecimal("17000"));
        sampleRequest.setStaffConditionGrade("GOOD");

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(bookItemRepository.save(any(BookItem.class))).thenAnswer(inv -> {
            BookItem bi = inv.getArgument(0);
            bi.setId(999L);
            return bi;
        });
        when(tradeInImageRepository.findByTradeInRequestId(10L)).thenReturn(List.of());
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(wishlistService.checkAndTriggerRestockAlerts(any(BookItem.class))).thenReturn(0);

        TradeInResponseDto result = tradeInService.confirmPhysicalReceiptAndStock(10L, "Kệ Thu Mua - T1", sampleStaff);

        assertNotNull(result);
        assertEquals("RECEIVED_STOCKED", result.getStatus());
        verify(bookItemRepository, times(1)).save(any(BookItem.class));
        verify(wishlistService, times(1)).checkAndTriggerRestockAlerts(any(BookItem.class));
    }

    @Test
    @DisplayName("Không thể nhập kho nếu phiếu không ở trạng thái ACCEPTED_AWAITING_DELIVERY (FR-TRD-06)")
    void testConfirmPhysicalReceiptAndStock_WrongStatus_ThrowsException() {
        sampleRequest.setStatus("PENDING_REVIEW"); // Chưa qua bước báo giá

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tradeInService.confirmPhysicalReceiptAndStock(10L, "Kệ T1", sampleStaff));
        assertTrue(ex.getMessage().contains("ACCEPTED_AWAITING_DELIVERY"));
    }

    @Test
    @DisplayName("Nhập kho thành công kích hoạt Wishlist Alert cho độc giả đang chờ sách (FR-TRD-06 + FR-WSH-02)")
    void testConfirmPhysicalReceiptAndStock_TriggersWishlistAlert() {
        sampleRequest.setStatus("ACCEPTED_AWAITING_DELIVERY");
        sampleRequest.setStaffReviewedPrice(new BigDecimal("20000"));
        sampleRequest.setStaffConditionGrade("VERY_GOOD");

        when(tradeInRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(bookItemRepository.save(any(BookItem.class))).thenAnswer(inv -> {
            BookItem bi = inv.getArgument(0);
            bi.setId(1001L);
            return bi;
        });
        when(tradeInImageRepository.findByTradeInRequestId(10L)).thenReturn(List.of());
        when(tradeInRequestRepository.save(any(TradeInRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(wishlistService.checkAndTriggerRestockAlerts(any(BookItem.class))).thenReturn(3);

        tradeInService.confirmPhysicalReceiptAndStock(10L, "Kệ Thu Mua", sampleStaff);

        // Xác nhận wishlist alert được gọi để thông báo cho độc giả đang chờ
        verify(wishlistService, times(1)).checkAndTriggerRestockAlerts(any(BookItem.class));
    }
}
