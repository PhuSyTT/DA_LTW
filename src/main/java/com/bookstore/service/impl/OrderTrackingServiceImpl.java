package com.bookstore.service.impl;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.OrderTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderTrackingServiceImpl implements OrderTrackingService {

    private final SubOrderRepository subOrderRepository;
    private final MasterOrderRepository masterOrderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final BranchRepository branchRepository;
    private final BookItemRepository bookItemRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public OrderTrackingServiceImpl(SubOrderRepository subOrderRepository,
                                   MasterOrderRepository masterOrderRepository,
                                   OrderStatusHistoryRepository orderStatusHistoryRepository,
                                   BranchRepository branchRepository,
                                   BookItemRepository bookItemRepository,
                                   UserRepository userRepository) {
        this.subOrderRepository = subOrderRepository;
        this.masterOrderRepository = masterOrderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.branchRepository = branchRepository;
        this.bookItemRepository = bookItemRepository;
        this.userRepository = userRepository;
    }

    // =========================================================================
    // 1. DÀNH CHO KHÁCH HÀNG (CUSTOMER)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubOrderDto> getCustomerSubOrders(Long userId) {
        List<SubOrder> subOrders = subOrderRepository.findByUserIdOrderByIdDesc(userId);
        return subOrders.stream().map(this::mapToSubOrderDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingTimelineDto getSubOrderTimeline(Long subOrderId, Long currentUserId, boolean isStaffOrAdmin) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã ID: " + subOrderId));

        // Phân quyền: Nếu không phải nhân viên/admin thì phải đúng là người đặt đơn
        if (!isStaffOrAdmin) {
            MasterOrder master = subOrder.getMasterOrder();
            if (master == null || master.getUser() == null || !master.getUser().getId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền truy cập vào thông tin đơn hàng này!");
            }
        }

        OrderTrackingTimelineDto dto = new OrderTrackingTimelineDto();
        dto.setSubOrderId(subOrder.getId());
        dto.setSubOrderCode(subOrder.getSubOrderCode());

        MasterOrder master = subOrder.getMasterOrder();
        if (master != null) {
            dto.setMasterOrderId(master.getId());
            dto.setMasterOrderCode(master.getMasterOrderCode());
            dto.setReceiverName(master.getReceiverName());
            dto.setReceiverPhone(master.getReceiverPhone());
            dto.setShippingAddress(master.getShippingAddress());
            dto.setPaymentMethod(master.getPaymentMethod());
            dto.setPaymentStatus(master.getPaymentStatus());
        }

        Branch branch = subOrder.getBranch();
        if (branch != null) {
            dto.setBranchId(branch.getId());
            dto.setBranchName(branch.getBranchName());
            dto.setBranchAddress(branch.getAddress());
            dto.setBranchPhone(branch.getPhone());
        }

        dto.setCurrentStatus(subOrder.getStatus());
        dto.setStatusLabel(getStatusLabel(subOrder.getStatus()));
        dto.setStatusBadgeClass(getStatusBadgeClass(subOrder.getStatus()));

        dto.setBranchSubtotal(subOrder.getBranchSubtotal() != null ? subOrder.getBranchSubtotal() : BigDecimal.ZERO);
        dto.setBranchShippingFee(subOrder.getBranchShippingFee() != null ? subOrder.getBranchShippingFee() : BigDecimal.ZERO);
        dto.setTotalAmount(dto.getBranchSubtotal().add(dto.getBranchShippingFee()));

        dto.setTrackingNumber(subOrder.getTrackingNumber());
        dto.setCreatedAt(subOrder.getCreatedAt());
        dto.setConfirmedAt(subOrder.getConfirmedAt());
        dto.setShippedAt(subOrder.getShippedAt());
        dto.setCompletedAt(subOrder.getCompletedAt());
        dto.setCancelledAt(subOrder.getCancelledAt());
        dto.setCancelReason(subOrder.getCancelReason());

        // Driver & Delivery Info
        if (subOrder.getDriver() != null) {
            dto.setDriverId(subOrder.getDriver().getId());
        }
        dto.setShipperName(subOrder.getShipperName());
        dto.setShipperPhone(subOrder.getShipperPhone());
        dto.setShipperVehicle(subOrder.getShipperVehicle());
        dto.setShippingCarrier(subOrder.getShippingCarrier());
        dto.setDeliveryStatus(subOrder.getDeliveryStatus());
        dto.setEstimatedDeliveryTime(subOrder.getEstimatedDeliveryTime());
        dto.setDeliveryNotes(subOrder.getDeliveryNotes());

        // Khách hàng có quyền hủy khi trạng thái là PENDING
        dto.setCanCancel("PENDING".equalsIgnoreCase(subOrder.getStatus()));

        // Map items
        if (subOrder.getItems() != null) {
            dto.setItems(subOrder.getItems().stream().map(this::mapToItemDto).collect(Collectors.toList()));
        }

        // Lấy lịch sử log từ CSDL
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findBySubOrderIdOrderByCreatedAtAsc(subOrderId);
        dto.setHistoryLogs(histories.stream().map(this::mapToHistoryDto).collect(Collectors.toList()));

        // Dựng timeline 5 bước trực quan
        dto.setTimelineSteps(buildTimelineSteps(subOrder, histories));

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubOrderByCustomer(Long subOrderId, String cancelReason, Long userId) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã ID: " + subOrderId));

        MasterOrder master = subOrder.getMasterOrder();
        if (master == null || master.getUser() == null || !master.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác trên đơn hàng này!");
        }

        if (!"PENDING".equalsIgnoreCase(subOrder.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn khi đơn hàng đang ở trạng thái 'Chờ xác nhận' (PENDING)!");
        }

        String reason = (cancelReason != null && !cancelReason.isBlank()) ? cancelReason.trim() : "Khách hàng yêu cầu hủy đơn";

        // 1. Hoàn lại số lượng tồn kho cho các sản phẩm trong đơn
        if (subOrder.getItems() != null) {
            for (SubOrderItem item : subOrder.getItems()) {
                BookItem bookItem = item.getBookItem();
                if (bookItem != null) {
                    int restoredQuantity = (bookItem.getStockQuantity() != null ? bookItem.getStockQuantity() : 0) + item.getQuantity();
                    bookItem.setStockQuantity(restoredQuantity);
                    bookItem.setStatus("AVAILABLE");
                    bookItemRepository.save(bookItem);
                }
            }
        }

        // 2. Cập nhật trạng thái sub_order
        subOrder.setStatus("CANCELLED");
        subOrder.setCancelledAt(LocalDateTime.now());
        subOrder.setCancelReason(reason);
        subOrder.setUpdatedAt(LocalDateTime.now());
        subOrderRepository.save(subOrder);

        // 3. Ghi log lịch sử thay đổi trạng thái
        User user = userRepository.findById(userId).orElse(null);
        OrderStatusHistory history = new OrderStatusHistory(
                subOrder,
                master,
                "PENDING",
                "CANCELLED",
                "Khách hàng hủy đơn: " + reason,
                user,
                subOrder.getBranch(),
                subOrder.getBranch(),
                "ORDER_CANCELLED"
        );
        orderStatusHistoryRepository.save(history);
    }

    // =========================================================================
    // 2. DÀNH CHO QUẢN LÝ CHI NHÁNH (BRANCH MANAGER)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubOrderDto> getBranchSubOrders(Long branchId, String status, String keyword, String fromDate, String toDate) {
        String cleanStatus = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status.toUpperCase().trim() : null;
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        List<SubOrder> list = subOrderRepository.filterBranchOrders(branchId, cleanStatus, cleanKeyword);

        // Lọc thêm theo ngày nếu người dùng chọn
        if (fromDate != null && !fromDate.isBlank()) {
            LocalDate start = LocalDate.parse(fromDate);
            list = list.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().toLocalDate().isBefore(start)).collect(Collectors.toList());
        }
        if (toDate != null && !toDate.isBlank()) {
            LocalDate end = LocalDate.parse(toDate);
            list = list.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().toLocalDate().isAfter(end)).collect(Collectors.toList());
        }

        return list.stream().map(this::mapToSubOrderDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FulfillmentStatsDto getBranchDailyStats(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với ID: " + branchId));

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        long pending = subOrderRepository.countByBranchIdAndStatus(branchId, "PENDING");
        long packing = subOrderRepository.countByBranchIdAndStatus(branchId, "PACKING");
        long shipped = subOrderRepository.countByBranchIdAndStatus(branchId, "SHIPPED");
        long completed = subOrderRepository.countByBranchIdAndStatus(branchId, "COMPLETED");
        long cancelled = subOrderRepository.countByBranchIdAndStatus(branchId, "CANCELLED");

        return new FulfillmentStatsDto(branchId, branch.getBranchName(), pending, packing, shipped, completed, cancelled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubOrderDto updateStatusByBranchManager(Long subOrderId, String newStatus, String note, String trackingNumber, User manager) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));

        // Kiểm tra thẩm quyền chi nhánh (trừ khi là ADMIN)
        boolean isAdmin = manager != null && manager.getRole() != null &&
                (manager.getRole().getRoleName().contains("ADMIN"));
        if (!isAdmin && manager != null && manager.getBranch() != null) {
            if (!subOrder.getBranch().getId().equals(manager.getBranch().getId())) {
                throw new RuntimeException("Bạn chỉ có quyền quản lý đơn hàng thuộc chi nhánh của mình!");
            }
        }

        String currentStatus = subOrder.getStatus();
        String targetStatus = newStatus.toUpperCase().trim();

        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("Đơn hàng đã ở trạng thái kết thúc (" + currentStatus + "), không thể thay đổi!");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("CANCELLED".equals(targetStatus)) {
            // Hủy đơn & hoàn tồn kho
            if (subOrder.getItems() != null) {
                for (SubOrderItem item : subOrder.getItems()) {
                    BookItem bookItem = item.getBookItem();
                    if (bookItem != null) {
                        int restoredQuantity = (bookItem.getStockQuantity() != null ? bookItem.getStockQuantity() : 0) + item.getQuantity();
                        bookItem.setStockQuantity(restoredQuantity);
                        bookItem.setStatus("AVAILABLE");
                        bookItemRepository.save(bookItem);
                    }
                }
            }
            subOrder.setStatus("CANCELLED");
            subOrder.setCancelledAt(now);
            subOrder.setCancelReason(note != null ? note : "Quản lý chi nhánh hủy đơn");
        } else {
            subOrder.setStatus(targetStatus);
            if ("CONFIRMED".equals(targetStatus)) {
                subOrder.setConfirmedAt(now);
            } else if ("PACKING".equals(targetStatus)) {
                if (subOrder.getConfirmedAt() == null) {
                    subOrder.setConfirmedAt(now);
                }
            } else if ("SHIPPED".equals(targetStatus)) {
                subOrder.setShippedAt(now);
                if (trackingNumber != null && !trackingNumber.isBlank()) {
                    subOrder.setTrackingNumber(trackingNumber.trim());
                } else if (subOrder.getTrackingNumber() == null || subOrder.getTrackingNumber().isBlank()) {
                    subOrder.setTrackingNumber("VNPOST-" + System.currentTimeMillis());
                }
            } else if ("COMPLETED".equals(targetStatus)) {
                subOrder.setCompletedAt(now);
            }
        }

        subOrder.setUpdatedAt(now);
        SubOrder saved = subOrderRepository.save(subOrder);

        // Ghi log lịch sử thao tác
        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                saved.getMasterOrder(),
                currentStatus,
                targetStatus,
                note != null ? note : ("Quản lý chi nhánh chuyển trạng thái sang " + targetStatus),
                manager,
                saved.getBranch(),
                saved.getBranch(),
                "STATUS_CHANGE"
        );
        orderStatusHistoryRepository.save(history);

        return mapToSubOrderDto(saved);
    }

    // =========================================================================
    // 3. DÀNH CHO TỔNG QUẢN TRỊ (SUPER ADMIN)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubOrderDto> getAllSubOrdersForAdmin(Long branchId, String status, String keyword, String fromDate, String toDate) {
        String cleanStatus = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status.toUpperCase().trim() : null;
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        List<SubOrder> list = subOrderRepository.filterAllOrders(branchId, cleanStatus, cleanKeyword);

        if (fromDate != null && !fromDate.isBlank()) {
            LocalDate start = LocalDate.parse(fromDate);
            list = list.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().toLocalDate().isBefore(start)).collect(Collectors.toList());
        }
        if (toDate != null && !toDate.isBlank()) {
            LocalDate end = LocalDate.parse(toDate);
            list = list.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().toLocalDate().isAfter(end)).collect(Collectors.toList());
        }

        return list.stream().map(this::mapToSubOrderDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDashboardDto getAdminDashboard() {
        AdminOrderDashboardDto dashboard = new AdminOrderDashboardDto();

        List<Branch> branches = branchRepository.findAll();
        List<BranchLoadStatsDto> branchLoads = new ArrayList<>();

        long totalAll = 0;
        long totalPendingAll = 0;
        long totalProcessingAll = 0;
        long totalCompletedAll = 0;
        long totalCancelledAll = 0;

        for (Branch b : branches) {
            BranchLoadStatsDto load = new BranchLoadStatsDto();
            load.setBranchId(b.getId());
            load.setBranchName(b.getBranchName());
            load.setBranchAddress(b.getAddress());
            load.setBranchPhone(b.getPhone());

            long pending = subOrderRepository.countByBranchIdAndStatus(b.getId(), "PENDING");
            long confirmed = subOrderRepository.countByBranchIdAndStatus(b.getId(), "CONFIRMED");
            long packing = subOrderRepository.countByBranchIdAndStatus(b.getId(), "PACKING");
            long shipped = subOrderRepository.countByBranchIdAndStatus(b.getId(), "SHIPPED");
            long completed = subOrderRepository.countByBranchIdAndStatus(b.getId(), "COMPLETED");
            long cancelled = subOrderRepository.countByBranchIdAndStatus(b.getId(), "CANCELLED");

            long processing = pending + confirmed + packing + shipped;
            long branchTotal = processing + completed + cancelled;

            load.setPendingCount(pending);
            load.setConfirmedCount(confirmed);
            load.setPackingCount(packing);
            load.setShippedCount(shipped);
            load.setCompletedCount(completed);
            load.setCancelledCount(cancelled);
            load.setProcessingCount(processing);
            load.setTotalCount(branchTotal);

            // Cảnh báo quá tải nếu đang có hơn 5 đơn hàng đang xử lý dồn ứ
            load.setOverloaded(processing >= 5);
            load.setLoadPercentage(branchTotal > 0 ? (int) Math.min(100, (processing * 100) / Math.max(1, branchTotal)) : 0);

            branchLoads.add(load);

            totalAll += branchTotal;
            totalPendingAll += pending;
            totalProcessingAll += processing;
            totalCompletedAll += completed;
            totalCancelledAll += cancelled;
        }

        dashboard.setBranchLoads(branchLoads);
        dashboard.setTotalSubOrders(totalAll);
        dashboard.setTotalPending(totalPendingAll);
        dashboard.setTotalProcessing(totalProcessingAll);
        dashboard.setTotalCompleted(totalCompletedAll);
        dashboard.setTotalCancelled(totalCancelledAll);
        dashboard.setAvgCompletionRate(totalAll > 0 ? Math.round(((double) totalCompletedAll / totalAll) * 1000.0) / 10.0 : 0.0);

        // Lấy 20 lịch sử log gần nhất toàn hệ thống
        List<OrderStatusHistory> recentLogs = orderStatusHistoryRepository.findTop100ByOrderByCreatedAtDesc();
        dashboard.setRecentAuditLogs(recentLogs.stream().limit(20).map(this::mapToHistoryDto).collect(Collectors.toList()));

        return dashboard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubOrderDto reassignBranch(Long subOrderId, Long newBranchId, String reason, User admin) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng con với ID: " + subOrderId));

        if ("COMPLETED".equals(subOrder.getStatus()) || "CANCELLED".equals(subOrder.getStatus())) {
            throw new RuntimeException("Đơn hàng đã hoàn thành hoặc đã hủy, không thể điều phối!");
        }

        Branch oldBranch = subOrder.getBranch();
        Branch newBranch = branchRepository.findById(newBranchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh đích với ID: " + newBranchId));

        if (oldBranch != null && oldBranch.getId().equals(newBranchId)) {
            throw new RuntimeException("Đơn hàng hiện tại đã thuộc về chi nhánh " + newBranch.getBranchName() + "!");
        }

        String dispatchReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Quản trị viên điều phối chi nhánh để tối ưu tiến độ đóng gói";

        // Cập nhật chi nhánh phụ trách mới
        subOrder.setBranch(newBranch);
        subOrder.setUpdatedAt(LocalDateTime.now());
        SubOrder saved = subOrderRepository.save(subOrder);

        // Ghi log kiểm toán (audit log)
        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                saved.getMasterOrder(),
                saved.getStatus(),
                saved.getStatus(),
                "Điều phối đơn hàng: [" + (oldBranch != null ? oldBranch.getBranchName() : "N/A") +
                        "] ➔ [" + newBranch.getBranchName() + "]. Lý do: " + dispatchReason,
                admin,
                oldBranch,
                newBranch,
                "BRANCH_REASSIGN"
        );
        orderStatusHistoryRepository.save(history);

        return mapToSubOrderDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDto> getSubOrderAuditLogs(Long subOrderId) {
        List<OrderStatusHistory> list = orderStatusHistoryRepository.findBySubOrderIdOrderByCreatedAtDesc(subOrderId);
        return list.stream().map(this::mapToHistoryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDto> getAllRecentAuditLogs() {
        List<OrderStatusHistory> list = orderStatusHistoryRepository.findTop100ByOrderByCreatedAtDesc();
        return list.stream().map(this::mapToHistoryDto).collect(Collectors.toList());
    }

    // =========================================================================
    // HELPER & MAPPING METHODS
    // =========================================================================

    private List<OrderTimelineStepDto> buildTimelineSteps(SubOrder subOrder, List<OrderStatusHistory> histories) {
        List<OrderTimelineStepDto> steps = new ArrayList<>();
        String status = subOrder.getStatus();

        // 1. Chờ xác nhận
        LocalDateTime t1 = subOrder.getCreatedAt();
        boolean c1 = true;
        boolean cur1 = "PENDING".equalsIgnoreCase(status);
        steps.add(new OrderTimelineStepDto(1, "PENDING", "Chờ xác nhận", "Đơn hàng đã được tạo và gửi tới chi nhánh", "fa-clock", t1, formatTime(t1), c1, cur1, false));

        // Nếu đơn hàng bị hủy khi chưa sang bước nào khác
        if ("CANCELLED".equalsIgnoreCase(status)) {
            LocalDateTime tc = subOrder.getCancelledAt() != null ? subOrder.getCancelledAt() : subOrder.getUpdatedAt();
            steps.add(new OrderTimelineStepDto(2, "CANCELLED", "Đã hủy đơn", subOrder.getCancelReason() != null ? subOrder.getCancelReason() : "Đơn hàng đã hủy", "fa-ban", tc, formatTime(tc), true, true, true));
            return steps;
        }

        // 2. Đã xác nhận
        LocalDateTime t2 = subOrder.getConfirmedAt();
        boolean c2 = hasReachedStatus(status, "CONFIRMED");
        boolean cur2 = "CONFIRMED".equalsIgnoreCase(status);
        steps.add(new OrderTimelineStepDto(2, "CONFIRMED", "Đã xác nhận", "Chi nhánh đã tiếp nhận và xác nhận đơn hàng", "fa-clipboard-check", t2, formatTime(t2), c2, cur2, false));

        // 3. Đang đóng gói
        LocalDateTime t3 = findStatusTime(histories, "PACKING", subOrder.getUpdatedAt());
        boolean c3 = hasReachedStatus(status, "PACKING");
        boolean cur3 = "PACKING".equalsIgnoreCase(status);
        steps.add(new OrderTimelineStepDto(3, "PACKING", "Đang đóng gói", "Thủ kho đang lấy sách tại kệ và đóng gói đơn", "fa-boxes-packing", t3, formatTime(t3), c3, cur3, false));

        // 4. Đang giao hàng
        LocalDateTime t4 = subOrder.getShippedAt();
        boolean c4 = hasReachedStatus(status, "SHIPPED");
        boolean cur4 = "SHIPPED".equalsIgnoreCase(status);
        String shipDesc = (subOrder.getTrackingNumber() != null && !subOrder.getTrackingNumber().isBlank())
                ? "Bưu tá đã nhận hàng. Mã vận đơn: " + subOrder.getTrackingNumber()
                : "Đơn hàng đã được bàn giao cho đối tác vận chuyển";
        steps.add(new OrderTimelineStepDto(4, "SHIPPED", "Đang giao hàng", shipDesc, "fa-truck-fast", t4, formatTime(t4), c4, cur4, false));

        // 5. Giao thành công
        LocalDateTime t5 = subOrder.getCompletedAt();
        boolean c5 = "COMPLETED".equalsIgnoreCase(status);
        boolean cur5 = c5;
        steps.add(new OrderTimelineStepDto(5, "COMPLETED", "Giao thành công", "Khách hàng đã nhận được sách nguyên vẹn", "fa-circle-check", t5, formatTime(t5), c5, cur5, false));

        return steps;
    }

    private boolean hasReachedStatus(String currentStatus, String targetStatus) {
        List<String> order = List.of("PENDING", "CONFIRMED", "PACKING", "SHIPPED", "COMPLETED");
        int curIdx = order.indexOf(currentStatus);
        int targetIdx = order.indexOf(targetStatus);
        return curIdx >= targetIdx && curIdx != -1;
    }

    private LocalDateTime findStatusTime(List<OrderStatusHistory> histories, String targetStatus, LocalDateTime fallback) {
        if (histories == null) return null;
        for (OrderStatusHistory h : histories) {
            if (targetStatus.equalsIgnoreCase(h.getToStatus())) {
                return h.getCreatedAt();
            }
        }
        return null;
    }

    private String formatTime(LocalDateTime dt) {
        return dt != null ? dt.format(TIME_FORMATTER) : null;
    }

    private SubOrderDto mapToSubOrderDto(SubOrder entity) {
        SubOrderDto dto = new SubOrderDto();
        dto.setId(entity.getId());
        dto.setSubOrderCode(entity.getSubOrderCode());

        if (entity.getMasterOrder() != null) {
            MasterOrder master = entity.getMasterOrder();
            dto.setMasterOrderId(master.getId());
            dto.setMasterOrderCode(master.getMasterOrderCode());
            dto.setCustomerName(master.getReceiverName());
            dto.setCustomerPhone(master.getReceiverPhone());
            if (master.getUser() != null) {
                dto.setCustomerEmail(master.getUser().getEmail());
            }
        }

        if (entity.getBranch() != null) {
            dto.setBranchId(entity.getBranch().getId());
            dto.setBranchName(entity.getBranch().getBranchName());
            dto.setBranchAddress(entity.getBranch().getAddress());
        }

        dto.setBranchSubtotal(entity.getBranchSubtotal() != null ? entity.getBranchSubtotal() : BigDecimal.ZERO);
        dto.setBranchShippingFee(entity.getBranchShippingFee() != null ? entity.getBranchShippingFee() : BigDecimal.ZERO);
        dto.setTotalAmount(dto.getBranchSubtotal().add(dto.getBranchShippingFee()));

        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Driver & Delivery Info
        if (entity.getDriver() != null) {
            dto.setDriverId(entity.getDriver().getId());
        }
        dto.setShipperName(entity.getShipperName());
        dto.setShipperPhone(entity.getShipperPhone());
        dto.setShipperVehicle(entity.getShipperVehicle());
        dto.setShippingCarrier(entity.getShippingCarrier());
        dto.setDeliveryStatus(entity.getDeliveryStatus());
        dto.setEstimatedDeliveryTime(entity.getEstimatedDeliveryTime() != null ? entity.getEstimatedDeliveryTime().toString() : null);
        dto.setDeliveryNotes(entity.getDeliveryNotes());

        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(this::mapToItemDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private SubOrderItemDto mapToItemDto(SubOrderItem item) {
        SubOrderItemDto dto = new SubOrderItemDto();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());

        BookItem bookItem = item.getBookItem();
        if (bookItem != null) {
            dto.setBookItemId(bookItem.getId());
            dto.setSkuBarcode(bookItem.getSkuBarcode());
            dto.setConditionGrade(bookItem.getConditionGrade());
            dto.setShelfLocation(bookItem.getShelfLocation());

            if (bookItem.getBook() != null) {
                dto.setBookTitle(bookItem.getBook().getTitle());
                dto.setBookCoverImage(bookItem.getBook().getCoverImageUrl());
                if (bookItem.getBook().getAuthor() != null) {
                    dto.setAuthorName(bookItem.getBook().getAuthor().getName());
                }
            }
        }
        return dto;
    }

    private OrderStatusHistoryDto mapToHistoryDto(OrderStatusHistory history) {
        OrderStatusHistoryDto dto = new OrderStatusHistoryDto();
        dto.setId(history.getId());
        dto.setFromStatus(history.getFromStatus());
        dto.setToStatus(history.getToStatus());
        dto.setToStatusLabel(getStatusLabel(history.getToStatus()));
        dto.setNote(history.getNote());
        dto.setActionType(history.getActionType());
        dto.setActionTypeLabel(getActionTypeLabel(history.getActionType()));
        dto.setCreatedAt(history.getCreatedAt());

        if (history.getSubOrder() != null) {
            dto.setSubOrderId(history.getSubOrder().getId());
            dto.setSubOrderCode(history.getSubOrder().getSubOrderCode());
        }

        if (history.getMasterOrder() != null) {
            dto.setMasterOrderId(history.getMasterOrder().getId());
            dto.setMasterOrderCode(history.getMasterOrder().getMasterOrderCode());
        }

        if (history.getUpdatedByUser() != null) {
            dto.setUpdatedByUserId(history.getUpdatedByUser().getId());
            dto.setUpdatedByUserName(history.getUpdatedByUser().getFullName());
            if (history.getUpdatedByUser().getRole() != null) {
                dto.setUpdatedByUserRole(history.getUpdatedByUser().getRole().getRoleName());
            }
        }

        if (history.getFromBranch() != null) {
            dto.setFromBranchId(history.getFromBranch().getId());
            dto.setFromBranchName(history.getFromBranch().getBranchName());
        }

        if (history.getToBranch() != null) {
            dto.setToBranchId(history.getToBranch().getId());
            dto.setToBranchName(history.getToBranch().getBranchName());
        }

        return dto;
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toUpperCase()) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "PACKING": return "Đang đóng gói";
            case "SHIPPED": return "Đang giao hàng";
            case "COMPLETED": return "Giao thành công";
            case "CANCELLED": return "Đã hủy đơn";
            default: return status;
        }
    }

    private String getStatusBadgeClass(String status) {
        if (status == null) return "bg-warning text-dark";
        switch (status.toUpperCase()) {
            case "PENDING": return "bg-warning text-dark";
            case "CONFIRMED": return "bg-primary text-white";
            case "PACKING": return "bg-info text-dark";
            case "SHIPPED": return "bg-primary text-white";
            case "COMPLETED": return "bg-success text-white";
            case "CANCELLED": return "bg-danger text-white";
            default: return "bg-secondary text-white";
        }
    }

    private String getActionTypeLabel(String actionType) {
        if (actionType == null) return "Thay đổi trạng thái";
        switch (actionType) {
            case "STATUS_CHANGE": return "Đổi tiến độ";
            case "BRANCH_REASSIGN": return "Điều phối chi nhánh";
            case "ORDER_CANCELLED": return "Hủy đơn hàng";
            default: return actionType;
        }
    }
}

