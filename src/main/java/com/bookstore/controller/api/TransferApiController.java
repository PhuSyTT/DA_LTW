package com.bookstore.controller.api;

import com.bookstore.dto.TransferRequestDto;
import com.bookstore.entity.StockTransfer;
import com.bookstore.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferApiController {

    private final TransferService transferService;

    public TransferApiController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTransfer(@RequestBody TransferRequestDto request) {
        try {
            // Thực tế sẽ lấy thông tin Branch Manager từ SecurityContext
            StockTransfer transfer = transferService.createTransferRequest(request, null);
            return ResponseEntity.ok("Tạo phiếu điều chuyển thành công: " + transfer.getTransferCode());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/export")
    public ResponseEntity<?> exportTransfer(@PathVariable Long id) {
        try {
            transferService.exportTransfer(id);
            return ResponseEntity.ok("Xuất kho thành công, hàng đang luân chuyển.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<?> receiveTransfer(@PathVariable Long id) {
        try {
            transferService.receiveTransfer(id);
            return ResponseEntity.ok("Đã nhận hàng và cộng tồn kho chi nhánh đích.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
 // Thêm các endpoint này vào trong class TransferApiController

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllTransfers() {
        try {
            // Trong thực tế cần kiểm tra quyền ADMIN bằng Spring Security ở đây
            return ResponseEntity.ok(transferService.getAllTransfersForAdmin());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getTransferStats() {
        try {
            return ResponseEntity.ok(transferService.getTransferStatistics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}