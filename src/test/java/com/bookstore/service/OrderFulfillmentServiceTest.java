package com.bookstore.service;

import com.bookstore.dto.FulfillmentStatsDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.dto.UpdateSubOrderStatusDto;
import com.bookstore.entity.*;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.SubOrderRepository;
import com.bookstore.service.impl.OrderFulfillmentServiceImpl;
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

@ExtendWith(MockitoExtension.class)
class OrderFulfillmentServiceTest {

    @Mock
    private SubOrderRepository subOrderRepository;

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private OrderFulfillmentServiceImpl fulfillmentService;

    private Branch sampleBranch;
    private SubOrder sampleSubOrder;
    private BookItem sampleBookItem;

    @BeforeEach
    void setUp() {
        sampleBranch = new Branch(1L, "Chi nhánh Quận 1", "123 Lê Lợi", "0901234567", "Hồ Chí Minh", null, null, true);

        Book sampleBook = Book.builder()
                .id(1L)
                .title("Dế Mèn Phiêu Lưu Ký")
                .originalPrice(new BigDecimal("90000"))
                .build();

        sampleBookItem = new BookItem(10L, sampleBook, sampleBranch, "Q1-DMN-001", "LIKE_NEW",
                new BigDecimal("75000"), 0, "Kệ Thiếu Nhi - Dãy B1", "Rất mới", null, "SOLD");

        sampleSubOrder = new SubOrder();
        sampleSubOrder.setId(100L);
        sampleSubOrder.setSubOrderCode("SUB-Q1-100");
        sampleSubOrder.setBranch(sampleBranch);
        sampleSubOrder.setBranchSubtotal(new BigDecimal("75000"));
        sampleSubOrder.setBranchShippingFee(new BigDecimal("15000"));
        sampleSubOrder.setStatus("PENDING");

        SubOrderItem orderItem = new SubOrderItem();
        orderItem.setId(1L);
        orderItem.setSubOrder(sampleSubOrder);
        orderItem.setBookItem(sampleBookItem);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(new BigDecimal("75000"));
        orderItem.setSubtotal(new BigDecimal("75000"));

        List<SubOrderItem> items = new ArrayList<>();
        items.add(orderItem);
        sampleSubOrder.setItems(items);
    }

    @Test
    @DisplayName("Cập nhật trạng thái sang SHIPPED và sinh mã vận đơn thành công")
    void testUpdateStatusToShipped() {
        when(subOrderRepository.findById(100L)).thenReturn(Optional.of(sampleSubOrder));
        when(subOrderRepository.save(any(SubOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSubOrderStatusDto dto = new UpdateSubOrderStatusDto("SHIPPED", "VNPOST-123456", "Đã bàn giao bưu tá");
        SubOrderDto result = fulfillmentService.updateSubOrderStatus(100L, dto, null);

        assertNotNull(result);
        assertEquals("SHIPPED", result.getStatus());
        assertEquals("VNPOST-123456", result.getTrackingNumber());
        verify(subOrderRepository, times(1)).save(sampleSubOrder);
    }

    @Test
    @DisplayName("Hủy đơn hàng và tự động hoàn lại tồn kho (FR-FUL-04)")
    void testCancelAndRestockSubOrder() {
        when(subOrderRepository.findById(100L)).thenReturn(Optional.of(sampleSubOrder));
        when(subOrderRepository.save(any(SubOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fulfillmentService.cancelAndRestockSubOrder(100L, "Khách đổi ý", null);

        assertEquals("CANCELLED", sampleSubOrder.getStatus());
        assertEquals(1, sampleBookItem.getStockQuantity());
        assertEquals("AVAILABLE", sampleBookItem.getStatus());
        verify(bookItemRepository, times(1)).save(sampleBookItem);
        verify(subOrderRepository, times(1)).save(sampleSubOrder);
    }

    @Test
    @DisplayName("Thống kê số lượng đơn hàng theo từng trạng thái tại chi nhánh")
    void testGetBranchFulfillmentStats() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(sampleBranch));
        when(subOrderRepository.countByBranchIdAndStatus(1L, "PENDING")).thenReturn(5L);
        when(subOrderRepository.countByBranchIdAndStatus(1L, "PACKING")).thenReturn(3L);
        when(subOrderRepository.countByBranchIdAndStatus(1L, "SHIPPED")).thenReturn(2L);
        when(subOrderRepository.countByBranchIdAndStatus(1L, "COMPLETED")).thenReturn(10L);
        when(subOrderRepository.countByBranchIdAndStatus(1L, "CANCELLED")).thenReturn(1L);

        FulfillmentStatsDto stats = fulfillmentService.getBranchFulfillmentStats(1L);

        assertNotNull(stats);
        assertEquals(1L, stats.getBranchId());
        assertEquals(5L, stats.getPendingCount());
        assertEquals(3L, stats.getPackingCount());
        assertEquals(2L, stats.getShippedCount());
        assertEquals(10L, stats.getCompletedCount());
        assertEquals(21L, stats.getTotalCount());
    }
}
