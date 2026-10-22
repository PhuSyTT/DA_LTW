package com.bookstore.repository;

import com.bookstore.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    // Lấy lịch sử theo session ID cho khách vãng lai
    List<AiChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    // Lấy lịch sử theo User ID cho khách hàng đã đăng nhập
    List<AiChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    // Xóa lịch sử chat theo session ID
    void deleteBySessionId(String sessionId);

    // Xóa lịch sử chat theo user ID
    void deleteByUserId(Long userId);
}

