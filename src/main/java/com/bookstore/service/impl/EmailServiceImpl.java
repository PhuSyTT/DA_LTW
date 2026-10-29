package com.bookstore.service.impl;

import com.bookstore.entity.User;
import com.bookstore.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.name:Book4Life}")
    private String appName;

    @Value("${mail.from.email:book4lifeute@gmail.com}")
    private String fromEmail;

    @Value("${support.hotline.email:book4lifeute@gmail.com}")
    private String hotlineEmail;

    @Value("${brevo.api.key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendRegistrationOtp(String toEmail, String otpCode) {
        sendOtp(toEmail, otpCode, "đăng ký tài khoản");
    }

    @Override
    @Async
    public void sendOtp(String toEmail, String otpCode, String purposeText) {
        try {
            String purpose = (purposeText != null && !purposeText.isBlank()) ? purposeText : "xác thực";
            // Định dạng mã OTP hiển thị 3 số cách 3 số: "123 456"
            String formattedOtp = (otpCode != null && otpCode.length() == 6)
                    ? otpCode.substring(0, 3) + " " + otpCode.substring(3, 6)
                    : otpCode;

            String subject = "[" + appName + "] - " + otpCode + " là mã xác nhận " + purpose + " của bạn";
            String htmlBody = buildOtpEmailHtml(formattedOtp, purpose);

            // Ưu tiên 1: Gửi qua Brevo REST API (Cổng HTTPS 443 - 100% không bị Render chặn)
            if (sendViaBrevoHttpApi(toEmail, subject, htmlBody)) {
                log.info("Đã gửi thành công email chứa mã OTP xác thực ({}) qua Brevo REST API (HTTPS/443) tới: {}", purpose, toEmail);
                return;
            }

            // Ưu tiên 2: Gửi qua JavaMailSender (SMTP)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Đã gửi thành công email chứa mã OTP xác thực ({}) qua SMTP tới: {}", purpose, toEmail);
        } catch (Exception ex) {
            log.error("Lỗi khi gửi email OTP tới {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Không thể gửi email OTP qua máy chủ SMTP/API: " + ex.getMessage());
        }
    }

    private String buildOtpEmailHtml(String formattedOtp, String purpose) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #FAF9F6; margin: 0; padding: 24px; color: #2D261E; }"
                + "  .card { max-width: 540px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #E8E0D5; padding: 36px 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }"
                + "  .header-brand { font-size: 22px; font-weight: 800; color: #835500; letter-spacing: 0.5px; margin-bottom: 24px; text-transform: uppercase; }"
                + "  p { line-height: 1.6; font-size: 14px; margin: 0 0 14px 0; }"
                + "  .otp-box { background: #FFF9F0; border: 2px dashed #D7C3AE; border-radius: 12px; padding: 20px; text-align: center; margin: 26px 0; }"
                + "  .otp-code { font-size: 36px; font-weight: 800; color: #835500; letter-spacing: 8px; font-family: 'Courier New', Courier, monospace; display: inline-block; }"
                + "  .timer-badge { font-size: 13px; color: #BA1A1A; font-weight: 600; margin-top: 10px; }"
                + "  .security-alert { background: #FDF3F2; border-left: 4px solid #BA1A1A; border-radius: 6px; padding: 14px 18px; margin: 24px 0; font-size: 13px; line-height: 1.6; color: #5B211F; }"
                + "  .footer { border-top: 1px solid #EFEFEF; padding-top: 20px; margin-top: 28px; font-size: 12px; color: #7F8C8D; line-height: 1.6; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class=\"card\">"
                + "    <div class=\"header-brand\">" + appName + "</div>"
                + "    <p>Xin chào,</p>"
                + "    <p>Chúng tôi vừa nhận được yêu cầu <strong>" + purpose + "</strong> tại <strong>[" + appName + "]</strong> liên kết với địa chỉ email này.</p>"
                + "    <p>Để hoàn tất xác nhận, vui lòng nhập mã OTP dưới đây:</p>"
                + "    <div class=\"otp-box\">"
                + "      <div class=\"otp-code\">" + formattedOtp + "</div>"
                + "      <div class=\"timer-badge\">⏱️ Mã này chỉ có hiệu lực trong vòng 5 phút.</div>"
                + "    </div>"
                + "    <div class=\"security-alert\">"
                + "      <strong>🔒 Lưu ý bảo mật:</strong><br>"
                + "      • Tuyệt đối <strong>KHÔNG</strong> chia sẻ mã này cho bất kỳ ai, kể cả nhân viên hỗ trợ của chúng tôi.<br>"
                + "      • Nếu bạn không yêu cầu thao tác này, xin vui lòng bỏ qua và đổi mật khẩu để bảo vệ tài khoản."
                + "    </div>"
                + "    <p style=\"font-size: 13px;\">Cần hỗ trợ? Truy cập <a href=\"#\" style=\"color: #835500; font-weight: 600;\">[Trung tâm trợ giúp]</a> hoặc phản hồi lại email này.</p>"
                + "    <div class=\"footer\">"
                + "      Trân trọng,<br>"
                + "      <strong>Đội ngũ [" + appName + "]</strong>"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    @Override
    @Async
    public void sendCustomerSupportQuery(String customerQuery, User user, String sessionId) {
        try {
            String customerName = user != null && user.getFullName() != null ? user.getFullName() : "Khách vãng lai";
            String customerEmail = user != null && user.getEmail() != null ? user.getEmail() : "Chưa đăng nhập (Khách trực tuyến)";
            String customerPhone = user != null && user.getPhone() != null ? user.getPhone() : "Chưa cập nhật";
            String userRole = (user != null && user.getRole() != null) ? user.getRole().getRoleName() : "Khách chưa đăng ký";

            String previewText = customerQuery.length() > 50 ? customerQuery.substring(0, 47) + "..." : customerQuery;
            String subject = "[" + appName + " Hotline] - Yêu cầu hỗ trợ từ " + customerName + ": \"" + previewText + "\"";

            String htmlBody = buildSupportQueryEmailHtml(customerQuery, customerName, customerEmail, customerPhone, userRole, sessionId);

            // Ưu tiên 1: Gửi qua Brevo REST API (Cổng HTTPS 443)
            if (sendViaBrevoHttpApi(hotlineEmail, subject, htmlBody)) {
                log.info("Đã chuyển tiếp thành công câu hỏi của khách hàng [{}] tới hotline email qua Brevo REST API (HTTPS/443)", customerName);
                return;
            }

            // Ưu tiên 2: Fallback qua JavaMailSender (SMTP)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, appName + " CSKH");
            helper.setTo(hotlineEmail);
            if (user != null && user.getEmail() != null && user.getEmail().contains("@")) {
                helper.setReplyTo(user.getEmail());
            }
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Đã chuyển tiếp thành công câu hỏi của khách hàng [{}] tới hotline email: {}", customerName, hotlineEmail);
        } catch (Exception ex) {
            log.error("Lỗi khi gửi email chuyển tiếp câu hỏi CSKH tới {}: {}", hotlineEmail, ex.getMessage());
        }
    }

    private String buildSupportQueryEmailHtml(String customerQuery, String customerName, String customerEmail, String customerPhone, String userRole, String sessionId) {
        String currentTime = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy").format(LocalDateTime.now());
        String escapedQuery = customerQuery != null ? customerQuery.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>") : "";

        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #FAF9F6; margin: 0; padding: 24px; color: #2D261E; }"
                + "  .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #E8E0D5; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }"
                + "  .header-brand { font-size: 20px; font-weight: 800; color: #835500; letter-spacing: 0.5px; margin-bottom: 6px; text-transform: uppercase; }"
                + "  .header-sub { font-size: 13px; color: #7F8C8D; margin-bottom: 24px; border-bottom: 1px solid #EFEFEF; padding-bottom: 16px; }"
                + "  .info-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; font-size: 13px; }"
                + "  .info-table td { padding: 8px 12px; border-bottom: 1px solid #F4F1EA; }"
                + "  .info-table td.label { font-weight: 700; color: #6A5E52; width: 140px; background: #FAF9F6; border-radius: 6px; }"
                + "  .info-table td.value { color: #2D261E; }"
                + "  .query-box { background: #FFF9F0; border-left: 4px solid #835500; border-radius: 8px; padding: 18px 20px; margin: 20px 0; font-size: 14px; line-height: 1.6; color: #2D261E; }"
                + "  .query-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #835500; margin-bottom: 8px; }"
                + "  .reply-btn { display: inline-block; background: #835500; color: #ffffff !important; text-decoration: none; padding: 10px 24px; border-radius: 8px; font-weight: 600; font-size: 13px; margin-top: 8px; }"
                + "  .footer { border-top: 1px solid #EFEFEF; padding-top: 18px; margin-top: 24px; font-size: 12px; color: #95A5A6; line-height: 1.6; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class=\"card\">"
                + "    <div class=\"header-brand\">" + appName + " • Hotline CSKH</div>"
                + "    <div class=\"header-sub\">Tiếp nhận câu hỏi hỗ trợ trực tuyến từ khung chat website</div>"
                + "    <table class=\"info-table\">"
                + "      <tr><td class=\"label\">👤 Khách hàng:</td><td class=\"value\"><strong>" + customerName + "</strong></td></tr>"
                + "      <tr><td class=\"label\">📧 Email:</td><td class=\"value\">" + customerEmail + "</td></tr>"
                + "      <tr><td class=\"label\">📞 Số điện thoại:</td><td class=\"value\">" + customerPhone + "</td></tr>"
                + "      <tr><td class=\"label\">🏷️ Vai trò:</td><td class=\"value\">" + userRole + "</td></tr>"
                + "      <tr><td class=\"label\">⏱️ Thời gian gửi:</td><td class=\"value\">" + currentTime + "</td></tr>"
                + "      <tr><td class=\"label\">🔑 Phiên Chat:</td><td class=\"value\"><code>" + sessionId + "</code></td></tr>"
                + "    </table>"
                + "    <div class=\"query-box\">"
                + "      <div class=\"query-title\">Nội Dung Câu Hỏi Cần Hỗ Trợ:</div>"
                + "      <div>" + escapedQuery + "</div>"
                + "    </div>"
                + (customerEmail.contains("@")
                    ? "    <div style=\"text-align: center; margin: 20px 0;\"><a href=\"mailto:" + customerEmail + "?subject=" + appName + "%20-%20Ph%E1%BA%A3n%20h%E1%BB%93i%20y%C3%AAu%20c%E1%BA%A7u%20h%E1%BB%97%20tr%E1%BB%A3\" class=\"reply-btn\">✉️ Trả Lời Khách Hàng Ngay</a></div>"
                    : "")
                + "    <div class=\"footer\">"
                + "      Tin nhắn này được gửi tự động từ <strong>Trợ lý ảo CSKH " + appName + "</strong>.<br/>"
                + "      Vui lòng kiểm tra và phản hồi lại khách hàng trong thời gian sớm nhất."
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    @Override
    @Async
    public void sendRestockAlertEmail(String toEmail, String customerName, String bookTitle, String authorName, String branchName, java.math.BigDecimal price, String grade, Long bookId) {
        try {
            String subject = "[" + appName + "] - Sách bạn quan tâm đã có hàng: \"" + bookTitle + "\"";
            String htmlBody = buildRestockAlertEmailHtml(customerName, bookTitle, authorName, branchName, price, grade, bookId);

            // Ưu tiên 1: Gửi qua Brevo REST API (Cổng HTTPS 443)
            if (sendViaBrevoHttpApi(toEmail, subject, htmlBody)) {
                log.info("Đã gửi thành công email Restock Alert qua Brevo REST API (HTTPS/443) tới: {} cho cuốn '{}'", toEmail, bookTitle);
                return;
            }

            // Ưu tiên 2: Fallback qua JavaMailSender (SMTP)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Đã gửi thành công email Restock Alert (Săn sách) tới: {} cho cuốn '{}'", toEmail, bookTitle);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email Restock Alert tới {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildRestockAlertEmailHtml(String customerName, String bookTitle, String authorName, String branchName, java.math.BigDecimal price, String grade, Long bookId) {
        String safeCustomer = (customerName != null && !customerName.isBlank()) ? customerName : "Bạn đọc yêu quý";
        String safeTitle = (bookTitle != null) ? bookTitle : "Cuốn sách bạn yêu thích";
        String safeAuthor = (authorName != null && !authorName.isBlank()) ? authorName : "Nhiều tác giả";
        String safeBranch = (branchName != null && !branchName.isBlank()) ? branchName : "Chuỗi cửa hàng Book4Life";
        String formattedPrice = (price != null) ? String.format("%,.0f ₫", price) : "Liên hệ";
        
        String gradeLabel;
        if ("LIKE_NEW".equalsIgnoreCase(grade)) gradeLabel = "Như Mới (Like New)";
        else if ("VERY_GOOD".equalsIgnoreCase(grade)) gradeLabel = "Rất Tốt (Very Good)";
        else if ("GOOD".equalsIgnoreCase(grade)) gradeLabel = "Tốt (Good)";
        else if ("ACCEPTABLE".equalsIgnoreCase(grade)) gradeLabel = "Chấp Nhận Được (Acceptable)";
        else gradeLabel = (grade != null) ? grade : "Đã qua kiểm định";

        String bookDetailUrl = "http://localhost:8080/books/" + (bookId != null ? bookId : "");

        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #FAF9F6; margin: 0; padding: 24px; color: #2D261E; }"
                + "  .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #E8E0D5; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }"
                + "  .header-brand { font-size: 20px; font-weight: 800; color: #835500; letter-spacing: 0.5px; margin-bottom: 6px; text-transform: uppercase; }"
                + "  .header-sub { font-size: 13px; color: #7F8C8D; margin-bottom: 24px; border-bottom: 1px solid #EFEFEF; padding-bottom: 16px; }"
                + "  .greeting { font-size: 15px; line-height: 1.6; margin-bottom: 20px; }"
                + "  .book-card { background: #FDFBF7; border: 1px solid #EAE3D2; border-radius: 12px; padding: 20px; margin: 20px 0; }"
                + "  .book-title { font-size: 18px; font-weight: 700; color: #2D261E; margin-bottom: 8px; }"
                + "  .book-meta { font-size: 13px; color: #6A5E52; margin-bottom: 6px; }"
                + "  .grade-badge { display: inline-block; background: #FFF3CD; color: #856404; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 20px; border: 1px solid #FFEEBA; }"
                + "  .price-tag { font-size: 20px; font-weight: 800; color: #B3541E; margin-top: 10px; }"
                + "  .action-btn { display: inline-block; background: #835500; color: #ffffff !important; text-decoration: none; padding: 12px 32px; border-radius: 8px; font-weight: 700; font-size: 14px; margin: 20px 0; text-align: center; box-shadow: 0 4px 12px rgba(131,85,0,0.25); }"
                + "  .footer { border-top: 1px solid #EFEFEF; padding-top: 18px; margin-top: 24px; font-size: 12px; color: #95A5A6; line-height: 1.6; text-align: center; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class=\"card\">"
                + "    <div class=\"header-brand\">📚 " + appName + " • Săn Sách Thành Công</div>"
                + "    <div class=\"header-sub\">Thông báo tự động từ tính năng Wishlist Săn Sách</div>"
                + "    <div class=\"greeting\">"
                + "      Xin chào <strong>" + safeCustomer + "</strong>,<br/>"
                + "      Cuốn sách cũ mà bạn đang tìm kiếm vừa được nhập kho và kiểm duyệt đạt tiêu chuẩn tại hệ thống của chúng tôi!"
                + "    </div>"
                + "    <div class=\"book-card\">"
                + "      <div class=\"book-title\">" + safeTitle + "</div>"
                + "      <div class=\"book-meta\">✍️ Tác giả: <strong>" + safeAuthor + "</strong></div>"
                + "      <div class=\"book-meta\">📍 Chi nhánh hiện có: <strong>" + safeBranch + "</strong></div>"
                + "      <div class=\"book-meta\" style=\"margin-top: 6px;\">🏷️ Tình trạng sách: <span class=\"grade-badge\">" + gradeLabel + "</span></div>"
                + "      <div class=\"price-tag\">Giá bán: " + formattedPrice + "</div>"
                + "    </div>"
                + "    <div style=\"text-align: center;\">"
                + "      <a href=\"" + bookDetailUrl + "\" class=\"action-btn\">⚡ Xem Chi Tiết & Đặt Mua Ngay</a>"
                + "      <div style=\"font-size: 12px; color: #888;\">Số lượng sách cũ có hạn, hãy nhanh tay đặt giữ trước khi hết hàng nhé!</div>"
                + "    </div>"
                + "    <div class=\"footer\">"
                + "      Bạn nhận được email này vì đã đăng ký thông báo Săn Sách trên <strong>" + appName + "</strong>.<br/>"
                + "      © 2026 " + appName + " - Nơi sách cũ tìm thấy độc giả mới."
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String customerName) {
        try {
            String safeName = (customerName != null && !customerName.isBlank()) ? customerName : "Bạn đọc yêu quý";
            String subject = "[" + appName + "] - Chào mừng " + safeName + " gia nhập Chuỗi Nhà Sách Book4Life!";
            String htmlBody = buildWelcomeEmailHtml(safeName, toEmail);

            // Ưu tiên 1: Gửi qua Brevo REST API (Cổng HTTPS 443)
            if (sendViaBrevoHttpApi(toEmail, subject, htmlBody)) {
                log.info("Đã gửi thành công email Chào mừng tài khoản mới qua Brevo REST API (HTTPS/443) tới: {}", toEmail);
                return;
            }

            // Ưu tiên 2: Fallback qua JavaMailSender (SMTP)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Đã gửi thành công email Chào mừng tài khoản mới tới: {}", toEmail);
        } catch (Exception ex) {
            log.error("Lỗi khi gửi email Chào mừng tài khoản mới tới {}: {}", toEmail, ex.getMessage());
        }
    }

    /**
     * Gửi email trực tiếp qua Brevo REST API (HTTPS - Cổng 443)
     * Vĩnh viễn không bị Render hay bất kỳ nhà cung cấp Cloud nào chặn cổng SMTP!
     */
    private boolean sendViaBrevoHttpApi(String toEmail, String subject, String htmlContent) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            return false;
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", appName);
            sender.put("email", fromEmail);
            payload.put("sender", sender);

            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            toList.add(recipient);
            payload.put("to", toList);

            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .header("content-type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("Đã gửi email thành công qua Brevo REST API (HTTPS cổng 443) tới: {}", toEmail);
                return true;
            } else {
                log.warn("Brevo REST API (Cổng 443) phản hồi mã lỗi [{}]: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception ex) {
            log.error("Lỗi khi kết nối tới Brevo REST API qua cổng 443: {}", ex.getMessage());
            return false;
        }
    }

    private String buildWelcomeEmailHtml(String customerName, String customerEmail) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\">"
                + "<style>"
                + "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #FAF9F6; margin: 0; padding: 24px; color: #2D261E; }"
                + "  .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #E8E0D5; padding: 36px 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }"
                + "  .header-brand { font-size: 22px; font-weight: 800; color: #835500; letter-spacing: 0.5px; margin-bottom: 8px; text-transform: uppercase; }"
                + "  .header-sub { font-size: 13px; color: #7F8C8D; margin-bottom: 24px; border-bottom: 1px solid #EFEFEF; padding-bottom: 16px; }"
                + "  p { line-height: 1.6; font-size: 14px; margin: 0 0 14px 0; }"
                + "  .feature-box { background: #FFF9F0; border-left: 4px solid #835500; border-radius: 8px; padding: 18px 20px; margin: 20px 0; font-size: 13px; line-height: 1.8; color: #2D261E; }"
                + "  .btn-explore { display: inline-block; background: #835500; color: #ffffff !important; text-decoration: none; padding: 12px 32px; border-radius: 8px; font-weight: 700; font-size: 14px; margin: 20px 0; text-align: center; box-shadow: 0 4px 12px rgba(131,85,0,0.25); }"
                + "  .footer { border-top: 1px solid #EFEFEF; padding-top: 20px; margin-top: 28px; font-size: 12px; color: #7F8C8D; line-height: 1.6; text-align: center; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class=\"card\">"
                + "    <div class=\"header-brand\">📚 " + appName + " • Chào Mừng Độc Giả Mới</div>"
                + "    <div class=\"header-sub\">Tài khoản của bạn đã được kích hoạt thành công trên hệ thống</div>"
                + "    <p>Xin chào <strong>" + customerName + "</strong>,</p>"
                + "    <p>Chúc mừng bạn đã tạo thành công tài khoản Độc giả tại <strong>[" + appName + "]</strong> với địa chỉ email: <strong>" + customerEmail + "</strong>.</p>"
                + "    <div class=\"feature-box\">"
                + "      <strong>✨ Các đặc quyền của bạn tại Book4Life:</strong><br/>"
                + "      • 📖 <strong>Khám phá hàng ngàn cuốn sách cũ tuyển chọn</strong> với giá tiết kiệm từ 30% - 70%.<br/>"
                + "      • 🔄 <strong>Chương trình Thu Cũ Đổi Mới (Trade-in):</strong> Định giá AI tự động, thanh lý sách cũ đổi sách mới.<br/>"
                + "      • 🔔 <strong>Săn Sách Wishlist:</strong> Nhận thông báo email ngay khi cuốn sách bạn mong muốn có hàng.<br/>"
                + "      • 🤖 <strong>Trợ lý ảo CSKH 24/7:</strong> Hỗ trợ tư vấn sách và tra cứu đơn hàng tức thì."
                + "    </div>"
                + "    <div style=\"text-align: center;\">"
                + "      <a href=\"http://localhost:8080/\" class=\"btn-explore\">🚀 Bắt Đầu Khám Phá Sách Ngay</a>"
                + "    </div>"
                + "    <div class=\"footer\">"
                + "      Email này được gửi tự động từ <strong>" + appName + "</strong> tới <strong>" + customerEmail + "</strong>.<br/>"
                + "      Chúc bạn có những trải nghiệm đọc sách tuyệt vời cùng chúng tôi!"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }
}
