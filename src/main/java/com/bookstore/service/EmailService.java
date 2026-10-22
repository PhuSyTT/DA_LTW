package com.bookstore.service;

import com.bookstore.entity.User;
import java.math.BigDecimal;

public interface EmailService {
    void sendRegistrationOtp(String toEmail, String otpCode);
    void sendCustomerSupportQuery(String customerQuery, User user, String sessionId);
    void sendRestockAlertEmail(String toEmail, String customerName, String bookTitle, String authorName, String branchName, BigDecimal price, String grade, Long bookId);
}
