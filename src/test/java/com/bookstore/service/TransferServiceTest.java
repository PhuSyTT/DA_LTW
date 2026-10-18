package com.bookstore.service;

import com.bookstore.dto.TransferRequestDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.Branch;
import com.bookstore.entity.StockTransfer;
import com.bookstore.entity.StockTransferItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.StockTransferRepository;
import com.bookstore.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private StockTransferRepository transferRepository;

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Branch branchSource;
    private Branch branchDest;
    private User manager;
    private BookItem regularBookItem;
    private BookItem rareBookItem;

    @BeforeEach
    void setUp() {
        branchSource = new Branch(1L, "Chi nhánh Quận 1", "120 Nguyễn Huệ", "028.3822.1101", "Hồ Chí Minh", null, null, true);
        branchDest = new Branch(2L, "Chi nhánh Thủ Đức", "45 Võ Văn Ngân", "028.3896.2202", "Hồ Chí Minh", null, null, true);
        manager = User.builder().id(2L).email("manager.q1@bookstore.vn").fullName("Trần Thị Lan").build();

        Book book = Book.builder().id(1L).title("Tuổi Thơ Dữ Dội").build();

        // Sách thông thường: nhập kho hơn 200 ngày, tồn kho 5 cuốn
        regularBookItem = new BookItem(10L, book, branchSource, "SKU-REG-01", "GOOD",
                new BigDecimal("80000"), 5, "Kệ A1", null, LocalDateTime.now().minusDays(200), "AVAILABLE");

        // Sách độc bản mới nhập: nhập kho 30 ngày (< 180 ngày), tồn kho đúng 1 cuốn
        rareBookItem = new BookItem(20L, book, branchSource, "SKU-RARE-99", "LIKE_NEW",
                new BigDecimal("250000"), 1, "Kệ Kính Hiếm", null, LocalDateTime.now().minusDays(30), "AVAILABLE");
    }

    @Test
    @DisplayName("FR-TRF-01: Tạo phiếu luân chuyển thành công với sách hợp lệ")
    void testCreateTransferRequest_Success() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromBranchId(1L);
        request.setToBranchId(2L);
        request.setReason("Cân đối kho hàng bán chạy");

        TransferRequestDto.TransferItemDto itemDto = new TransferRequestDto.TransferItemDto();
        itemDto.setBookItemId(10L);
        itemDto.setQuantity(2);
        request.setItems(Collections.singletonList(itemDto));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branchSource));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branchDest));
        when(bookItemRepository.findById(10L)).thenReturn(Optional.of(regularBookItem));
        when(transferRepository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockTransfer result = transferService.createTransferRequest(request, manager);

        assertNotNull(result);
        assertEquals("PENDING_APPROVAL", result.getStatus());
        assertEquals(branchSource, result.getFromBranch());
        assertEquals(branchDest, result.getToBranch());
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(transferRepository, times(1)).save(any(StockTransfer.class));
    }

    @Test
    @DisplayName("FR-TRF-02: Ràng buộc nghiệp vụ - Chặn luân chuyển sách độc bản mới nhập kho dưới 6 tháng")
    void testCreateTransferRequest_ViolationRareBookRule() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromBranchId(1L);
        request.setToBranchId(2L);
        request.setReason("Yêu cầu chuyển sách độc bản");

        TransferRequestDto.TransferItemDto itemDto = new TransferRequestDto.TransferItemDto();
        itemDto.setBookItemId(20L);
        itemDto.setQuantity(1);
        request.setItems(Collections.singletonList(itemDto));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branchSource));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branchDest));
        when(bookItemRepository.findById(20L)).thenReturn(Optional.of(rareBookItem));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.createTransferRequest(request, manager);
        });

        assertTrue(exception.getMessage().contains("Vi phạm ràng buộc: Sách SKU-RARE-99 là độc bản và mới nhập kho dưới 6 tháng"));
        verify(transferRepository, never()).save(any(StockTransfer.class));
    }

    @Test
    @DisplayName("FR-TRF-03: Xác nhận xuất kho tại nguồn -> chuyển sang IN_TRANSIT và trừ kho")
    void testExportTransfer_Success() {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(100L);
        transfer.setStatus("PENDING_APPROVAL");
        transfer.setFromBranch(branchSource);

        StockTransferItem item = new StockTransferItem();
        item.setStockTransfer(transfer);
        item.setBookItem(regularBookItem);
        item.setQuantity(2);

        List<StockTransferItem> items = new ArrayList<>();
        items.add(item);
        transfer.setItems(items);

        when(transferRepository.findById(100L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.exportTransfer(100L);

        assertEquals("IN_TRANSIT", transfer.getStatus());
        assertEquals(3, regularBookItem.getStockQuantity()); // 5 - 2 = 3
        verify(bookItemRepository, times(1)).save(regularBookItem);
        verify(transferRepository, times(1)).save(transfer);
    }

    @Test
    @DisplayName("FR-TRF-03: Tiếp nhận hàng tại đích -> chuyển sang COMPLETED, đổi sở hữu chi nhánh và cộng kho đích")
    void testReceiveTransfer_Success() {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(100L);
        transfer.setStatus("IN_TRANSIT");
        transfer.setFromBranch(branchSource);
        transfer.setToBranch(branchDest);

        StockTransferItem item = new StockTransferItem();
        item.setStockTransfer(transfer);
        item.setBookItem(regularBookItem);
        item.setQuantity(2);

        List<StockTransferItem> items = new ArrayList<>();
        items.add(item);
        transfer.setItems(items);

        when(transferRepository.findById(100L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.receiveTransfer(100L);

        assertEquals("COMPLETED", transfer.getStatus());
        assertNotNull(transfer.getCompletedAt());
        assertEquals(branchDest, regularBookItem.getBranch()); // Đổi chi nhánh sang chi nhánh đích
        assertEquals(7, regularBookItem.getStockQuantity()); // 5 + 2 = 7
        verify(bookItemRepository, times(1)).save(regularBookItem);
        verify(transferRepository, times(1)).save(transfer);
    }

    @Test
    @DisplayName("FR-TRF-04: Lấy danh sách toàn bộ phiếu luân chuyển cho Admin")
    void testGetAllTransfersForAdmin() {
        StockTransfer trf1 = new StockTransfer();
        trf1.setId(1L);
        StockTransfer trf2 = new StockTransfer();
        trf2.setId(2L);

        List<StockTransfer> list = new ArrayList<>();
        list.add(trf1);
        list.add(trf2);

        when(transferRepository.findAll()).thenReturn(list);

        List<StockTransfer> result = transferService.getAllTransfersForAdmin();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId()); // Mới nhất lên đầu (id DESC)
    }
}

