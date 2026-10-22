package com.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service định kỳ tự ping vào chính server (mỗi 12 phút)
 * Giúp Render Free Tier không bị chuyển sang trạng thái Sleep (ngủ đông sau 15 phút không có request).
 */
@Service
public class ServerKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(ServerKeepAliveService.class);

    // Render tự động inject biến môi trường RENDER_EXTERNAL_URL (ví dụ: https://book4life-store.onrender.com)
    @Value("${RENDER_EXTERNAL_URL:}")
    private String renderExternalUrl;

    @Value("${app.keep-alive.url:}")
    private String customPingUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Tự động ping mỗi 12 phút (720.000 ms).
     * Bắt đầu lần đầu tiên sau khi server chạy được 1 phút (initialDelay = 60.000 ms).
     */
    @Scheduled(fixedRate = 720000, initialDelay = 60000)
    public void selfPing() {
        String targetUrl = null;

        if (customPingUrl != null && !customPingUrl.isBlank()) {
            targetUrl = customPingUrl;
        } else if (renderExternalUrl != null && !renderExternalUrl.isBlank()) {
            targetUrl = renderExternalUrl.endsWith("/")
                    ? renderExternalUrl + "api/ping"
                    : renderExternalUrl + "/api/ping";
        }

        if (targetUrl == null || targetUrl.isBlank()) {
            log.info("[KEEP-ALIVE] Chưa phát hiện biến RENDER_EXTERNAL_URL (Đang chạy Local), bỏ qua self-ping.");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Book4Life-SelfPing-Bot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[KEEP-ALIVE] Ping thành công tới {} - HTTP Status: {}", targetUrl, response.statusCode());
        } catch (Exception e) {
            log.warn("[KEEP-ALIVE] Ping tới {} không thành công: {}", targetUrl, e.getMessage());
        }
    }
}
