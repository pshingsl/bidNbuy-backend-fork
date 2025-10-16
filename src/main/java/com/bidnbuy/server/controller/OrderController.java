package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.OrderRequestDto;
import com.bidnbuy.server.dto.OrderResponseDto;
import com.bidnbuy.server.dto.OrderUpdateRequestDto;
import com.bidnbuy.server.dto.OrderUpdateResponseDto;
import com.bidnbuy.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto dto) {
        OrderResponseDto response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            @RequestParam String type,
            @RequestParam(required = false) String status,
            @RequestHeader("X-USER-ID") Long userId // 📝 임시: 실제론 JWT에서 추출
    ) {
        List<OrderResponseDto> orders = orderService.getMyOrders(userId, type, status);
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("X-USER-ID") Long userId // 📝 임시: 실제론 JWT에서 추출
    ) {
        OrderResponseDto response = orderService.getOrderDetail(orderId, userId);
        return ResponseEntity.ok(response);
    }

    // 주문 상태 변경
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderUpdateResponseDto> updateOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-USER-ID") Long userId,  // JWT에서 추출 예정
            @RequestBody OrderUpdateRequestDto dto
    ) {
        OrderUpdateResponseDto response = orderService.updateOrderStatus(orderId, userId, dto);
        return ResponseEntity.ok(response);
    }




}
