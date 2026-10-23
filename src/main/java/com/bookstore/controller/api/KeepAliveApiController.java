package com.bookstore.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}

