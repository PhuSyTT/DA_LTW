package com.bookstore.controller.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class KeepAliveApiController {

    private final DataSource dataSource;

    @Value("${brevo.api.key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${spring.mail.host:smtp-relay.brevo.com}")
    private String mailHost;

    @Value("${spring.mail.port:2525}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${mail.from.email:book4lifeute@gmail.com}")
    private String fromEmail;

    public KeepAliveApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "UP");
        resp.put("message", "Book4Life Keep-Alive OK");
        resp.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Giữ kết nối CSDL Aiven luôn hoạt động, tránh bị Aiven tự động tắt do không có truy vấn (inactivity shutdown)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            resp.put("database", "CONNECTED");
        } catch (Exception e) {
            resp.put("database", "RECONNECTING");
        }

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam(value = "to", defaultValue = "dinhphusy001@gmail.com") String to) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("targetEmail", to);
        resp.put("mailHost", mailHost);
        resp.put("mailPort", mailPort);
        resp.put("mailUsername", mailUsername);
        resp.put("fromEmail", fromEmail);
        resp.put("hasBrevoApiKey", brevoApiKey != null && !brevoApiKey.isBlank());
        resp.put("brevoApiKeyPrefix", (brevoApiKey != null && brevoApiKey.length() > 8) ? brevoApiKey.substring(0, 8) + "..." : "NONE");

        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(10))
                        .build();

                String json = "{\"sender\":{\"name\":\"Book4Life\",\"email\":\"" + fromEmail + "\"},\"to\":[{\"email\":\"" + to + "\"}],\"subject\":\"[TEST LIVE] Brevo REST API\",\"htmlContent\":\"<p>Test Brevo Live API</p>\"}";

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("https://api.brevo.com/v3/smtp/email"))
                        .header("accept", "application/json")
                        .header("api-key", brevoApiKey.trim())
                        .header("content-type", "application/json")
                        .timeout(java.time.Duration.ofSeconds(15))
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                resp.put("apiStatusCode", response.statusCode());
                resp.put("apiResponseBody", response.body());
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    resp.put("finalStatus", "SUCCESS_VIA_HTTP_API");
                    return ResponseEntity.ok(resp);
                } else {
                    resp.put("finalStatus", "API_ERROR_" + response.statusCode());
                }
            } catch (Exception ex) {
                resp.put("apiException", ex.getMessage());
            }
        } else {
            resp.put("finalStatus", "NO_BREVO_API_KEY_FOUND");
        }

        return ResponseEntity.ok(resp);
    }
}

