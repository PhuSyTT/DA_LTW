package com.bookstore.service;

import com.bookstore.dto.ChatMessageDto;
import com.bookstore.dto.ChatRequestDto;
import com.bookstore.dto.ChatResponseDto;
import com.bookstore.dto.FaqItemDto;
import com.bookstore.entity.AiChatMessage;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.AiChatMessageRepository;
import com.bookstore.service.impl.AiChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private AiChatMessageRepository aiChatMessageRepository;

    @Mock
    private EmailService emailService;

    private AiChatServiceImpl aiChatService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        Role role = new Role(1L, "ROLE_CUSTOMER", "Khách hàng");
        sampleUser = new User(1L, "customer@gmail.com", "pass", "Nguyễn Văn Độc Giả", "0900000001", null, role, null, true, LocalDateTime.now());

        aiChatService = new AiChatServiceImpl(aiChatMessageRepository, emailService);
    }

    @Test
    @DisplayName("Xử lý tin nhắn khớp với kịch bản FAQ có sẵn (Rule-based) không cần gọi AI hay gửi email")
    void testProcessUserMessage_FaqMatch() {
        when(aiChatMessageRepository.save(any(AiChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatRequestDto req = new ChatRequestDto("Làm sao để tôi theo dõi tiến độ đơn hàng và hủy đơn?", "session-123");
        ChatResponseDto res = aiChatService.processUserMessage(req, sampleUser);

        assertNotNull(res);
        assertEquals("FAQ", res.getStatus());
        assertTrue(res.getReply().contains("Đơn Hàng"));
        // Xác nhận đã lưu cả tin nhắn User và tin nhắn Bot
        verify(aiChatMessageRepository, times(2)).save(any(AiChatMessage.class));
        // Không gửi email với câu hỏi có trong FAQ
        verify(emailService, never()).sendCustomerSupportQuery(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Xử lý câu hỏi tùy chỉnh: Chuyển tiếp email tới Hotline và trả về thông báo cho khách hàng")
    void testProcessUserMessage_CustomQuery_ForwardEmail() {
        when(aiChatMessageRepository.save(any(AiChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String customQuery = "Tôi muốn đặt mua số lượng 50 cuốn sách cũ làm từ thiện thì có chiết khấu thêm không?";
        ChatRequestDto req = new ChatRequestDto(customQuery, "session-123");
        ChatResponseDto res = aiChatService.processUserMessage(req, sampleUser);

        assertNotNull(res);
        assertEquals("FORWARDED_EMAIL", res.getStatus());
        assertEquals("Dạ, câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Chúng tôi sẽ phản hồi lại bạn qua Email đăng ký trong thời gian sớm nhất nhé!", res.getReply());

        // Kiểm tra đã gửi email chuyển tiếp
        verify(emailService, times(1)).sendCustomerSupportQuery(eq(customQuery), eq(sampleUser), eq("session-123"));
        // Xác nhận đã lưu tin nhắn vào database
        verify(aiChatMessageRepository, times(2)).save(any(AiChatMessage.class));
    }

    @Test
    @DisplayName("Lấy danh sách kịch bản FAQ")
    void testGetFaqList() {
        List<FaqItemDto> faqs = aiChatService.getFaqList();
        assertNotNull(faqs);
        assertFalse(faqs.isEmpty());
        assertTrue(faqs.stream().anyMatch(f -> f.getId().equals("trade-in")));
        assertTrue(faqs.stream().anyMatch(f -> f.getId().equals("order-tracking")));
    }

    @Test
    @DisplayName("Lấy lịch sử chat của khách hàng đã đăng nhập")
    void testGetChatHistory_ForUser() {
        AiChatMessage msg1 = new AiChatMessage(sampleUser, "session-123", "USER", "Xin chào");
        AiChatMessage msg2 = new AiChatMessage(sampleUser, "session-123", "ASSISTANT", "Dạ em chào bạn!");

        when(aiChatMessageRepository.findByUserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(msg1, msg2));

        List<ChatMessageDto> history = aiChatService.getChatHistory("session-123", sampleUser);

        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("USER", history.get(0).getSenderType());
        assertEquals("ASSISTANT", history.get(1).getSenderType());
    }

    @Test
    @DisplayName("Xóa lịch sử chat thành công")
    void testClearChatHistory() {
        aiChatService.clearChatHistory("session-123", sampleUser);
        verify(aiChatMessageRepository, times(1)).deleteByUserId(1L);
        verify(aiChatMessageRepository, times(1)).deleteBySessionId("session-123");
    }
}
