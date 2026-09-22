package com.bookstore.controller.api;

import com.bookstore.dto.CheckoutRequestDto;
import com.bookstore.entity.MasterOrder;
import com.bookstore.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDto request) {
        try {
            // Giả định User truyền vào là null hoặc lấy từ Security Context
            MasterOrder order = orderService.processCheckout(request, null);
            return ResponseEntity.ok("Tạo đơn thành công! Mã đơn: " + order.getMasterOrderCode());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}