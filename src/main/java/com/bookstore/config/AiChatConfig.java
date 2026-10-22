package com.bookstore.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

@Configuration
public class AiChatConfig {

    private static final Logger log = LoggerFactory.getLogger(AiChatConfig.class);

    @Value("${ai.gemini.api-key:${GEMINI_API_KEY:${AI_API_KEY:}}}")
    private String apiKey;

    @Value("${ai.gemini.model:${GEMINI_MODEL:gemini-flash-latest}}")
    private String model;

    @Value("${ai.mode:${AI_MODE:local}}")
    private String mode;

    @Value("${ai.system-prompt:}")
    private String customSystemPrompt;

    private static final String DEFAULT_SYSTEM_PROMPT =
            "Bạn là Trợ lý ảo Chăm Sóc Khách Hàng (CSKH) thông minh và thân thiện của Book4Life - " +
            "Hệ thống Chuỗi Cửa Hàng Sách Cũ Trực Tuyến & Bền Vững hàng đầu tại Việt Nam.\n\n" +
            "Nhiệm vụ của bạn:\n" +
            "1. Tư vấn tìm sách cũ, tình trạng sách (LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE, COLLECTIBLE), và giá bán tại các chi nhánh.\n" +
            "2. Hướng dẫn quy trình 'Thu Mua Sách Cũ' (Trade-in): Định giá tự động dựa trên bảng khảo sát độ mới, hỗ trợ mang đến chi nhánh hoặc gửi bưu điện.\n" +
            "3. Hướng dẫn theo dõi tiến độ đơn hàng: Khách hàng có thể truy cập mục 'Đơn Hàng Của Tôi' để xem hành trình đơn hàng qua timeline trực quan 5 bước, và có thể hủy đơn nếu đơn đang ở trạng thái 'Chờ xác nhận'.\n" +
            "4. Thông tin chuỗi chi nhánh: Chi nhánh Quận 1 (Trụ Sở - 120 Nguyễn Huệ), Chi nhánh Thủ Đức (45 Võ Văn Ngân), Chi nhánh Bình Thạnh (235 Bạch Đằng).\n\n" +
            "Nguyên tắc trả lời:\n" +
            "- Luôn dùng tiếng Việt, xưng hô 'em' hoặc 'Book4Life' và gọi khách là 'Anh/Chị' hoặc 'Bạn'.\n" +
            "- Trả lời lịch sự, ngắn gọn, súc tích, có cấu trúc gạch đầu dòng rõ ràng.\n" +
            "- Nếu câu hỏi ngoài phạm vi sách hoặc hệ thống, hãy từ chối khéo léo và hướng khách hàng về chủ đề sách và dịch vụ của Book4Life.";

    @PostConstruct
    public void init() {
        // Tự động nạp file .env ở thư mục gốc dự án nếu có
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                        continue;
                    }
                    int idx = line.indexOf('=');
                    String key = line.substring(0, idx).trim();
                    String val = line.substring(idx + 1).trim();
                    if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                        val = val.substring(1, val.length() - 1);
                    }
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, val);
                    }
                    if (("GEMINI_API_KEY".equalsIgnoreCase(key) || "AI_API_KEY".equalsIgnoreCase(key))
                            && (apiKey == null || apiKey.isBlank())) {
                        this.apiKey = val;
                    }
                    if ("GEMINI_MODEL".equalsIgnoreCase(key) && !val.isBlank()) {
                        this.model = val;
                    }
                    if ("AI_MODE".equalsIgnoreCase(key) && !val.isBlank()) {
                        this.mode = val.trim().toLowerCase();
                    }
                }
                log.info("Đã nạp thành công cấu hình môi trường từ file .env (Chế độ AI: {})", mode);
            } catch (Exception e) {
                log.warn("Không thể đọc file .env: {}", e.getMessage());
            }
        }

        if (isLocalMode()) {
            log.info("Chatbox AI đang chạy ở chế độ MÔ HÌNH TRỰC TIẾP TÍCH HỢP (In-App Local AI - Phản hồi tức thì 0.01s).");
        } else if (hasApiKey()) {
            log.info("Chatbox AI đã sẵn sàng với mô hình API: {}", model);
        } else {
            log.info("Chưa tìm thấy AI API Key. Chatbox AI tự động chuyển sang chế độ Mô hình Trực tiếp Tích hợp.");
        }
    }

    public String getMode() {
        return mode != null ? mode : "local";
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isLocalMode() {
        return "local".equalsIgnoreCase(mode) || "direct".equalsIgnoreCase(mode) || !hasApiKey();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        if (customSystemPrompt != null && !customSystemPrompt.isBlank()) {
            return customSystemPrompt;
        }
        return DEFAULT_SYSTEM_PROMPT;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.contains("YOUR_");
    }
}

