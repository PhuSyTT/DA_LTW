package com.bookstore.controller.api;

import com.bookstore.dto.FulfillmentStatsDto;
import com.bookstore.dto.SubOrderDto;
import com.bookstore.dto.UpdateSubOrderStatusDto;
import com.bookstore.entity.User;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.OrderFulfillmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fulfillment")
public class OrderFulfillmentApiController {

    private final OrderFulfillmentService fulfillmentService;

    public OrderFulfillmentApiController(OrderFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    // Lấy danh sách đơn hàng con theo chi nhánh và trạng thái
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<SubOrderDto>> getOrdersByBranch(
            @PathVariable Long branchId,
            @RequestParam(name = "status", required = false) String status) {
        List<SubOrderDto> orders = fulfillmentService.getOrdersByBranch(branchId, status);
        return ResponseEntity.ok(orders);
    }

    // Chi tiết đơn hàng con kèm vị trí kệ sách để nhặt hàng
    @GetMapping("/orders/{subOrderId}")
    public ResponseEntity<?> getSubOrderDetail(@PathVariable Long subOrderId) {
        try {
            SubOrderDto detail = fulfillmentService.getSubOrderDetail(subOrderId);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật trạng thái xử lý đơn hàng: PACKING -> SHIPPED -> COMPLETED
    @PutMapping("/orders/{subOrderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long subOrderId,
            @RequestBody UpdateSubOrderStatusDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            SubOrderDto updated = fulfillmentService.updateSubOrderStatus(subOrderId, dto, staff);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Nghiệp vụ: Phê duyệt đơn hàng (PENDING -> CONFIRMED)
    @PostMapping("/orders/{subOrderId}/approve")
    public ResponseEntity<?> approveOrder(
            @PathVariable Long subOrderId,
            @RequestParam(name = "note", required = false) String note,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            UpdateSubOrderStatusDto dto = new UpdateSubOrderStatusDto();
            dto.setStatus("CONFIRMED");
            dto.setNote(note != null && !note.isBlank() ? note.trim() : "Nhân viên/Quản lý chi nhánh phê duyệt đơn hàng");
            SubOrderDto updated = fulfillmentService.updateSubOrderStatus(subOrderId, dto, staff);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hủy đơn hàng và tự động hoàn kho (FR-FUL-04)
    @PostMapping("/orders/{subOrderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long subOrderId,
            @RequestParam(name = "reason", required = false, defaultValue = "Khách hàng yêu cầu hủy đơn") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            fulfillmentService.cancelAndRestockSubOrder(subOrderId, reason, staff);
            return ResponseEntity.ok("Hủy đơn hàng thành công và đã hoàn lại số lượng tồn kho!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Thống kê đơn hàng chi nhánh phục vụ Dashboard
    @GetMapping("/branch/{branchId}/stats")
    public ResponseEntity<FulfillmentStatsDto> getBranchStats(@PathVariable Long branchId) {
        FulfillmentStatsDto stats = fulfillmentService.getBranchFulfillmentStats(branchId);
        return ResponseEntity.ok(stats);
    }

    // Danh sách tài xế sẵn sàng theo chi nhánh
    @GetMapping("/branch/{branchId}/drivers")
    public ResponseEntity<?> getBranchDrivers(@PathVariable Long branchId) {
        return ResponseEntity.ok(fulfillmentService.getAvailableDrivers(branchId));
    }

    // Phân công tài xế giao hàng
    @PostMapping("/orders/{subOrderId}/assign-driver")
    public ResponseEntity<?> assignDriver(
            @PathVariable Long subOrderId,
            @RequestParam(name = "driverId", required = false) Long driverId,
            @RequestParam(name = "customShipperName", required = false) String customShipperName,
            @RequestParam(name = "customShipperPhone", required = false) String customShipperPhone,
            @RequestParam(name = "customLicensePlate", required = false) String customLicensePlate,
            @RequestParam(name = "carrierName", required = false, defaultValue = "Đội xe Book4Life") String carrierName,
            @RequestParam(name = "trackingNumber", required = false) String trackingNumber,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            SubOrderDto updated = fulfillmentService.assignDriver(
                    subOrderId, driverId, customShipperName, customShipperPhone,
                    customLicensePlate, carrierName, trackingNumber, notes, staff
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
