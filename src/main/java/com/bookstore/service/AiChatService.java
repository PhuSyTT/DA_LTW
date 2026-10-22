package com.bookstore.service;

import com.bookstore.dto.ChatMessageDto;
import com.bookstore.dto.ChatRequestDto;
import com.bookstore.dto.ChatResponseDto;
import com.bookstore.dto.FaqItemDto;
import com.bookstore.entity.User;

import java.util.List;

public interface AiChatService {

    ChatResponseDto processUserMessage(ChatRequestDto request, User currentUser);

    List<ChatMessageDto> getChatHistory(String sessionId, User currentUser);

    void clearChatHistory(String sessionId, User currentUser);

    List<FaqItemDto> getFaqList();
}

