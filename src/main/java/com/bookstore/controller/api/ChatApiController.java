package com.bookstore.controller.api;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.ChatMessageDto;
import com.bookstore.dto.ChatRequestDto;
import com.bookstore.dto.ChatResponseDto;
import com.bookstore.entity.User;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final AiChatService aiChatService;

    public ChatApiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Gửi tin nhắn đến Trợ lý ảo AI và nhận phản hồi
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody ChatRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            ChatResponseDto response = aiChatService.processUserMessage(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không thể xử lý tin nhắn: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách câu hỏi thường gặp (FAQ / Rule-based)
     * GET /api/chat/faq
     */
    @GetMapping("/faq")
    public ResponseEntity<?> getFaqList() {
        return ResponseEntity.ok(aiChatService.getFaqList());
    }

    /**
     * Lấy lịch sử hội thoại (theo user nếu đã đăng nhập, hoặc theo sessionId nếu là khách vãng lai)
     * GET /api/chat/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(name = "sessionId", required = false) String sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            List<ChatMessageDto> history = aiChatService.getChatHistory(sessionId, user);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi lấy lịch sử chat: " + e.getMessage()));
        }
    }

    /**
     * Xóa lịch sử hội thoại
     * DELETE /api/chat/history
     */
    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory(
            @RequestParam(name = "sessionId", required = false) String sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            aiChatService.clearChatHistory(sessionId, user);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa lịch sử chat thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi xóa lịch sử chat: " + e.getMessage()));
        }
    }
}

