package com.bookstore.controller.api;

import com.bookstore.dto.*;
import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.TradeInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade-in")
public class TradeInApiController {

    private final TradeInService tradeInService;
    private final UserRepository userRepository;
    private final com.bookstore.service.CloudinaryService cloudinaryService;

    public TradeInApiController(TradeInService tradeInService, UserRepository userRepository, com.bookstore.service.CloudinaryService cloudinaryService) {
        this.tradeInService = tradeInService;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // Tải ảnh thực tế của sách lên máy chủ / Cloudinary (FR-TRD-02)
    @PostMapping(value = "/upload-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTradeInImage(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            com.bookstore.service.CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "trade-in");
            return ResponseEntity.ok(java.util.Map.of(
                    "imageUrl", result.imageUrl(),
                    "publicId", result.publicId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải ảnh: " + e.getMessage());
        }
    }

    // Dự toán giá sơ bộ trực tiếp theo bảng khảo sát (FR-TRD-03)
    @PostMapping("/estimate")
    public ResponseEntity<?> calculateEstimate(@RequestBody AutoEstimateRequestDto request) {
        try {
            AutoEstimateResponseDto result = tradeInService.calculateAutoEstimate(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Gửi phiếu yêu cầu thu mua kèm ảnh (FR-TRD-01 & FR-TRD-02)
    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(
            @RequestBody TradeInCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            User user = null;
            if (userDetails != null) {
                user = userDetails.getUser();
            } else if (fallbackUserId != null) {
                user = userRepository.findById(fallbackUserId).orElse(null);
            }

            if (user == null) {
                user = userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Vui lòng đăng nhập để gửi yêu cầu!"));
            }

            TradeInResponseDto response = tradeInService.createTradeInRequest(dto, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xem chi tiết một phiếu thu mua
    @GetMapping("/requests/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            TradeInResponseDto response = tradeInService.getRequestById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Khách hàng xem lịch sử các phiếu thu mua của mình
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            Long userId = null;
            if (userDetails != null) {
                userId = userDetails.getUser().getId();
            } else if (fallbackUserId != null) {
                userId = fallbackUserId;
            } else {
                User demo = userRepository.findAll().stream().findFirst().orElse(null);
                if (demo != null) userId = demo.getId();
            }

            if (userId == null) {
                return ResponseEntity.ok(List.of());
            }

            List<TradeInResponseDto> list = tradeInService.getUserRequests(userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Staff Kiểm duyệt xem danh sách phiếu thuộc chi nhánh của mình
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getBranchRequests(
            @PathVariable Long branchId,
            @RequestParam(name = "status", required = false) String status) {
        try {
            List<TradeInResponseDto> list = tradeInService.getRequestsByBranch(branchId, status);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Staff Kiểm duyệt thẩm định và định giá lại (FR-TRD-04)
    @PutMapping("/requests/{id}/review")
    public ResponseEntity<?> reviewRequest(
            @PathVariable Long id,
            @RequestBody TradeInReviewDto reviewDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            TradeInResponseDto response = tradeInService.reviewTradeInRequest(id, reviewDto, staff);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Khách hàng phản hồi kết quả định giá (Đồng ý hoặc từ chối) (FR-TRD-05)
    @PutMapping("/requests/{id}/customer-response")
    public ResponseEntity<?> customerRespond(
            @PathVariable Long id,
            @RequestBody TradeInCustomerResponseDto responseDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = (userDetails != null) ? userDetails.getUser() : null;
            TradeInResponseDto response = tradeInService.customerRespondToOffer(id, responseDto, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Nhân viên kiểm duyệt xác nhận nhận sách thực tế & Nhập kho (FR-TRD-06)
    @PostMapping("/requests/{id}/confirm-stock")
    public ResponseEntity<?> confirmStock(
            @PathVariable Long id,
            @RequestParam(name = "shelfLocation", required = false, defaultValue = "Kệ Thu Mua - Dãy T1") String shelfLocation,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User staff = (userDetails != null) ? userDetails.getUser() : null;
            TradeInResponseDto response = tradeInService.confirmPhysicalReceiptAndStock(id, shelfLocation, staff);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
