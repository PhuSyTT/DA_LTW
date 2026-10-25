package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.impl.OrderTrackingServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTrackingServiceTest {

    @Mock
    private SubOrderRepository subOrderRepository;

    @Mock
    private MasterOrderRepository masterOrderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BookItemRepository bookItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private OrderTrackingServiceImpl orderTrackingService;

    private User sampleUser;
    private Branch branchQ1;
    private Branch branchTD;
    private MasterOrder sampleMasterOrder;
    private SubOrder sampleSubOrder;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role(1L, "ROLE_CUSTOMER", "Khách hàng");
        sampleUser = new User(100L, "customer@test.com", "pass", "Nguyễn Văn Độc Giả", "0912345678", null, customerRole, null, true, LocalDateTime.now());

        branchQ1 = new Branch(1L, "Chi nhánh Quận 1", "123 Lê Lợi, Q1", "0901234567", "Hồ Chí Minh", null, null, true);
        branchTD = new Branch(2L, "Chi nhánh Thủ Đức", "456 Võ Văn Ngân", "0907654321", "Hồ Chí Minh", null, null, true);

        sampleMasterOrder = new MasterOrder();
        sampleMasterOrder.setId(10L);
        sampleMasterOrder.setMasterOrderCode("MO-TEST-001");
        sampleMasterOrder.setUser(sampleUser);
        sampleMasterOrder.setReceiverName("Nguyễn Văn Độc Giả");
        sampleMasterOrder.setReceiverPhone("0912345678");
        sampleMasterOrder.setShippingAddress("123 Lê Lợi, Q1");

        sampleSubOrder = new SubOrder();
        sampleSubOrder.setId(50L);
        sampleSubOrder.setSubOrderCode("SUB-1-TEST");
        sampleSubOrder.setMasterOrder(sampleMasterOrder);
        sampleSubOrder.setBranch(branchQ1);
        sampleSubOrder.setStatus("PENDING");
        sampleSubOrder.setBranchSubtotal(new BigDecimal("150000"));
        sampleSubOrder.setBranchShippingFee(new BigDecimal("15000"));
        sampleSubOrder.setCreatedAt(LocalDateTime.now().minusHours(2));
    }

    @Test
    @DisplayName("Khách hàng xem timeline đơn hàng thành công")
    void testGetSubOrderTimeline() {
        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));
        when(orderStatusHistoryRepository.findBySubOrderIdOrderByCreatedAtAsc(50L)).thenReturn(new ArrayList<>());

        OrderTrackingTimelineDto timeline = orderTrackingService.getSubOrderTimeline(50L, 100L, false);

        assertNotNull(timeline);
        assertEquals("SUB-1-TEST", timeline.getSubOrderCode());
        assertEquals("PENDING", timeline.getCurrentStatus());
        assertTrue(timeline.isCanCancel()); // PENDING -> can cancel
        assertNotNull(timeline.getTimelineSteps());
        assertEquals(5, timeline.getTimelineSteps().size());
        assertEquals("Chờ xác nhận", timeline.getTimelineSteps().get(0).getTitle());
    }

    @Test
    @DisplayName("Khách hàng hủy đơn hàng ở trạng thái PENDING thành công")
    void testCancelSubOrderByCustomer() {
        Book sampleBook = new Book();
        sampleBook.setTitle("Dế Mèn Phiêu Lưu Ký");

        BookItem item = new BookItem();
        item.setId(1L);
        item.setStockQuantity(0);
        item.setStatus("SOLD");

        SubOrderItem subItem = new SubOrderItem();
        subItem.setBookItem(item);
        subItem.setQuantity(1);

        sampleSubOrder.setItems(List.of(subItem));

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(subOrderRepository.save(any(SubOrder.class))).thenReturn(sampleSubOrder);

        orderTrackingService.cancelSubOrderByCustomer(50L, "Tôi muốn đổi địa chỉ nhận sách", 100L);

        assertEquals("CANCELLED", sampleSubOrder.getStatus());
        assertEquals("Tôi muốn đổi địa chỉ nhận sách", sampleSubOrder.getCancelReason());
        assertEquals("AVAILABLE", item.getStatus());
        assertEquals(1, item.getStockQuantity());
        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Không cho phép khách hàng hủy đơn khi đã qua bước PENDING")
    void testCancelSubOrderFailsWhenNotPending() {
        sampleSubOrder.setStatus("PACKING");
        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderTrackingService.cancelSubOrderByCustomer(50L, "Hủy đơn", 100L));

        assertTrue(exception.getMessage().contains("Chờ xác nhận"));
    }

    @Test
    @DisplayName("Quản lý chi nhánh cập nhật trạng thái tiến độ đơn hàng")
    void testUpdateStatusByBranchManager() {
        User manager = new User();
        manager.setId(2L);
        manager.setBranch(branchQ1);
        Role managerRole = new Role(2L, "ROLE_BRANCH_MANAGER", "Quản lý chi nhánh");
        manager.setRole(managerRole);

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));
        when(subOrderRepository.save(any(SubOrder.class))).thenReturn(sampleSubOrder);

        SubOrderDto dto = orderTrackingService.updateStatusByBranchManager(
                50L, "CONFIRMED", "Đã tiếp nhận đơn hàng", null, manager);

        assertNotNull(dto);
        assertEquals("CONFIRMED", sampleSubOrder.getStatus());
        assertNotNull(sampleSubOrder.getConfirmedAt());
        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Super Admin điều phối đơn hàng sang chi nhánh khác")
    void testReassignBranch() {
        User admin = new User();
        admin.setId(1L);
        Role adminRole = new Role(1L, "ROLE_ADMIN", "Admin");
        admin.setRole(adminRole);

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branchTD));
        when(subOrderRepository.save(any(SubOrder.class))).thenReturn(sampleSubOrder);

        SubOrderDto dto = orderTrackingService.reassignBranch(50L, 2L, "Chi nhánh Q1 quá tải", admin);

        assertNotNull(dto);
        assertEquals(branchTD, sampleSubOrder.getBranch());
        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Super Admin xem Dashboard tổng quan tải chi nhánh")
    void testGetAdminDashboard() {
        when(branchRepository.findAll()).thenReturn(List.of(branchQ1, branchTD));
        when(subOrderRepository.countByBranchIdAndStatus(eq(1L), anyString())).thenReturn(2L);
        when(subOrderRepository.countByBranchIdAndStatus(eq(2L), anyString())).thenReturn(1L);
        when(orderStatusHistoryRepository.findTop100ByOrderByCreatedAtDesc()).thenReturn(new ArrayList<>());

        AdminOrderDashboardDto dashboard = orderTrackingService.getAdminDashboard();

        assertNotNull(dashboard);
        assertEquals(2, dashboard.getBranchLoads().size());
        assertNotNull(dashboard.getRecentAuditLogs());
    }

    @Test
    @DisplayName("Khách hàng xác nhận đã nhận hàng thành công kèm đánh giá sao")
    void testConfirmDeliveryByCustomer_Success() {
        sampleSubOrder.setStatus("SHIPPED");
        sampleMasterOrder.setPaymentStatus("UNPAID");

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(subOrderRepository.save(any(SubOrder.class))).thenReturn(sampleSubOrder);

        orderTrackingService.confirmDeliveryByCustomer(50L, 100L, "Sách rất đẹp, bọc cẩn thận", 5);

        assertEquals("COMPLETED", sampleSubOrder.getStatus());
        assertEquals("DELIVERED", sampleSubOrder.getDeliveryStatus());
        assertNotNull(sampleSubOrder.getCompletedAt());
        assertEquals("PAID", sampleMasterOrder.getPaymentStatus());
        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Khách hàng không thể xác nhận nhận hàng khi đơn chưa SHIPPED")
    void testConfirmDeliveryByCustomer_InvalidStatus() {
        sampleSubOrder.setStatus("PENDING");

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));

        assertThrows(RuntimeException.class, () ->
                orderTrackingService.confirmDeliveryByCustomer(50L, 100L, "Đã nhận", 5)
        );
    }

    @Test
    @DisplayName("Người dùng khác không thể xác nhận nhận hàng thay khách hàng chính chủ")
    void testConfirmDeliveryByCustomer_Unauthorized() {
        sampleSubOrder.setStatus("SHIPPED");

        when(subOrderRepository.findById(50L)).thenReturn(Optional.of(sampleSubOrder));

        assertThrows(RuntimeException.class, () ->
                orderTrackingService.confirmDeliveryByCustomer(50L, 999L, "Hack", 5)
        );
    }
}

