package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{orderId}/rating")
    public ResponseEntity<String> rateOrder(@PathVariable Long orderId, @RequestBody RatingRequest request, @AuthenticationPrincipal Long userId) {
        // user = buyer
        orderService.rateOrder(orderId, userId, request.getRating());
        return ResponseEntity.ok("별점이 등록되었습니다.");
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto dto) {
        OrderResponseDto response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    // 주문 전체 조회
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(@RequestParam String type, @RequestParam(required = false) String status, @AuthenticationPrincipal Long userId) {
        List<OrderResponseDto> orders = orderService.getMyOrders(userId, type, status);
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetail(@PathVariable Long orderId, @AuthenticationPrincipal Long userId) {
        OrderResponseDto response = orderService.getOrderDetail(orderId, userId);
        return ResponseEntity.ok(response);
    }

    // 주문 상태 변경
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderUpdateResponseDto> updateOrder(@PathVariable Long orderId, @AuthenticationPrincipal Long userId, @RequestBody OrderUpdateRequestDto dto) {
        OrderUpdateResponseDto response = orderService.updateOrderStatus(orderId, userId, dto);
        return ResponseEntity.ok(response);
    }




}
