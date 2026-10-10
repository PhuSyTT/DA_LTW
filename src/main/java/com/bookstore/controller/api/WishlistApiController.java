package com.bookstore.controller.api;

import com.bookstore.dto.WishlistRequestDto;
import com.bookstore.dto.WishlistResponseDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.User;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistApiController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    private final BookItemRepository bookItemRepository;

    public WishlistApiController(WishlistService wishlistService,
                                 UserRepository userRepository,
                                 BookItemRepository bookItemRepository) {
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
        this.bookItemRepository = bookItemRepository;
    }

    // Đăng ký Săn sách khi hết hàng (FR-WSH-01)
    @PostMapping
    public ResponseEntity<?> addToWishlist(
            @RequestBody WishlistRequestDto request,
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
                // Lấy user đầu tiên làm demo nếu chưa cấu hình JWT / session
                user = userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Vui lòng đăng nhập để thực hiện chức năng này!"));
            }

            WishlistResponseDto response = wishlistService.addToWishlist(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xem danh sách Săn sách cá nhân (FR-WSH-03)
    @GetMapping("/my")
    public ResponseEntity<?> getMyWishlists(
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

            List<WishlistResponseDto> list = wishlistService.getUserWishlists(userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa khỏi danh sách Săn sách (FR-WSH-03)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long id,
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
                throw new RuntimeException("Vui lòng đăng nhập để thực hiện!");
            }

            wishlistService.removeFromWishlist(id, userId);
            return ResponseEntity.ok("Đã hủy theo dõi cuốn sách này!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Kích hoạt thủ công kiểm tra và bắn thông báo sách về kho (FR-WSH-02)
    @PostMapping("/check-restock/{bookItemId}")
    public ResponseEntity<?> triggerRestockCheck(@PathVariable Long bookItemId) {
        try {
            BookItem item = bookItemRepository.findById(bookItemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng với ID: " + bookItemId));
            int count = wishlistService.checkAndTriggerRestockAlerts(item);
            return ResponseEntity.ok("Đã kích hoạt quét thông báo: " + count + " độc giả nhận được tin báo!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
