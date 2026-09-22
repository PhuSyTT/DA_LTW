package com.bookstore.controller.api;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.InventoryStatsDto;
import com.bookstore.dto.PriceRangeDto;
import com.bookstore.dto.ShelfLocationUpdateDto;
import com.bookstore.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryApiController {

    private final InventoryService inventoryService;

    public InventoryApiController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/suggest-sku")
    public ResponseEntity<ApiResponse<Map<String, String>>> suggestSku(
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "bookId", required = false) Long bookId,
            @RequestParam(name = "conditionGrade", required = false, defaultValue = "GOOD") String conditionGrade
    ) {
        String sku = inventoryService.generateSkuBarcode(branchId, bookId, conditionGrade);
        Map<String, String> res = new HashMap<>();
        res.put("skuBarcode", sku);
        return ResponseEntity.ok(ApiResponse.success(res, "Gợi ý mã SKU thành công"));
    }

    @GetMapping("/check-sku")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSku(
            @RequestParam("skuBarcode") String skuBarcode,
            @RequestParam(name = "excludeId", required = false) Long excludeId
    ) {
        boolean available = inventoryService.isSkuBarcodeAvailable(skuBarcode, excludeId);
        Map<String, Object> res = new HashMap<>();
        res.put("skuBarcode", skuBarcode);
        res.put("available", available);
        res.put("message", available ? "Mã SKU hợp lệ và chưa tồn tại" : "Mã SKU đã tồn tại trong hệ thống kho!");
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/suggest-price")
    public ResponseEntity<ApiResponse<PriceRangeDto>> suggestPrice(
            @RequestParam("bookId") Long bookId,
            @RequestParam(name = "conditionGrade", defaultValue = "GOOD") String conditionGrade
    ) {
        try {
            PriceRangeDto range = inventoryService.getSuggestedPriceRange(bookId, conditionGrade);
            return ResponseEntity.ok(ApiResponse.success(range, "Tính dải giá gợi ý thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @RequestMapping(value = "/{id}/shelf-location", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateShelfLocation(
            @PathVariable("id") Long id,
            @Valid @RequestBody ShelfLocationUpdateDto dto
    ) {
        try {
            boolean updated = inventoryService.updateShelfLocation(id, dto.getShelfLocation());
            Map<String, Object> res = new HashMap<>();
            res.put("id", id);
            res.put("shelfLocation", dto.getShelfLocation());
            res.put("updated", updated);
            return ResponseEntity.ok(ApiResponse.success(res, "Đã cập nhật vị trí kệ sách thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không thể cập nhật vị trí kệ: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<InventoryStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryStats()));
    }
}
