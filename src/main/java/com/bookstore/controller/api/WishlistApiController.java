package com.bookstore.controller.api;

import com.bookstore.dto.WishlistRequestDto;
import com.bookstore.dto.WishlistResponseDto;
import com.bookstore.entity.BookItem;
import com.bookstore.entity.User;
import com.bookstore.entity.Wishlist;
import com.bookstore.repository.BookItemRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistApiController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    private final BookItemRepository bookItemRepository;
    private final WishlistRepository wishlistRepository;

    public WishlistApiController(WishlistService wishlistService,
                                 UserRepository userRepository,
                                 BookItemRepository bookItemRepository,
                                 WishlistRepository wishlistRepository) {
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
        this.bookItemRepository = bookItemRepository;
        this.wishlistRepository = wishlistRepository;
    }

    // Đăng ký Săn sách khi hết hàng (FR-WSH-01)
    @PostMapping
    public ResponseEntity<?> addToWishlist(
            @RequestBody WishlistRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            User user = resolveUser(userDetails, fallbackUserId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "requiresLogin", true, "message", "Vui lòng đăng nhập để lưu sách vào danh sách yêu thích!"));
            }

            WishlistResponseDto response = wishlistService.addToWishlist(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Chuyển đổi trạng thái Yêu thích (Toggle Heart: Thêm/Xóa nhanh theo tài khoản)
    @PostMapping("/toggle/{bookId}")
    public ResponseEntity<?> toggleWishlist(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            User user = resolveUser(userDetails, fallbackUserId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "requiresLogin", true, "message", "Vui lòng đăng nhập để thêm vào danh sách yêu thích!"));
            }

            Optional<Wishlist> existingOpt = wishlistRepository.findByUserIdAndBookId(user.getId(), bookId);
            if (existingOpt.isPresent()) {
                wishlistRepository.delete(existingOpt.get());
                long count = wishlistRepository.countByUserId(user.getId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "inWishlist", false,
                        "count", count,
                        "message", "Đã xóa sách khỏi danh sách yêu thích"
                ));
            } else {
                WishlistRequestDto req = new WishlistRequestDto();
                req.setBookId(bookId);
                req.setMinConditionGrade("GOOD");
                wishlistService.addToWishlist(req, user);
                long count = wishlistRepository.countByUserId(user.getId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "inWishlist", true,
                        "count", count,
                        "message", "Đã thêm vào danh sách yêu thích của bạn!"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Kiểm tra xem cuốn sách đã nằm trong danh sách yêu thích của khách hay chưa
    @GetMapping("/status/{bookId}")
    public ResponseEntity<?> checkWishlistStatus(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        User user = resolveUser(userDetails, fallbackUserId);
        if (user == null) {
            return ResponseEntity.ok(Map.of("inWishlist", false, "isLoggedIn", false));
        }
        boolean exists = wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId);
        long count = wishlistRepository.countByUserId(user.getId());
        return ResponseEntity.ok(Map.of("inWishlist", exists, "count", count, "isLoggedIn", true));
    }

    // Xem danh sách Săn sách cá nhân theo tài khoản khách hàng (FR-WSH-03)
    @GetMapping("/my")
    public ResponseEntity<?> getMyWishlists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            User user = resolveUser(userDetails, fallbackUserId);
            if (user == null) {
                return ResponseEntity.ok(List.of());
            }

            List<WishlistResponseDto> list = wishlistService.getUserWishlists(user.getId());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Xóa khỏi danh sách Săn sách (FR-WSH-03)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "userId", required = false) Long fallbackUserId) {
        try {
            User user = resolveUser(userDetails, fallbackUserId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Vui lòng đăng nhập để thực hiện!"));
            }

            wishlistService.removeFromWishlist(id, user.getId());
            long count = wishlistRepository.countByUserId(user.getId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã hủy theo dõi cuốn sách này!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
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

    private User resolveUser(CustomUserDetails userDetails, Long fallbackUserId) {
        if (userDetails != null && userDetails.getUser() != null) {
            return userDetails.getUser();
        }
        if (fallbackUserId != null) {
            return userRepository.findById(fallbackUserId).orElse(null);
        }
        return null;
    }
}
