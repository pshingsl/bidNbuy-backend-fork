package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주문 API", description = "주문 기능 제공")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "별점 생성", description = "주문에 대한 별점 생성 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "별점 등록 성공",
                    content = @Content(schema = @Schema(type = "string", implementation = RatingRequest.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "주문 정보를 찾을 수 없음") // 예외적으로 추가 고려
    })
    @PostMapping("/{orderId}/rating")
    public ResponseEntity<String> rateOrder(@PathVariable Long orderId, @RequestBody RatingRequest request, @AuthenticationPrincipal Long userId) {
        // user = buyer
        orderService.rateOrder(orderId, userId, request.getRating());
        return ResponseEntity.ok("별점이 등록되었습니다.");
    }

    // 주문 생성
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))), // Response DTO로 변경
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터") // 예외적으로 추가 고려
    })
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto dto) {
        OrderResponseDto response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    // 주문 전체 조회
    @Operation(summary = "주문 조회", description = "주문 조회시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderListResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(@RequestParam String type, @RequestParam(required = false) String status, @AuthenticationPrincipal Long userId) {
        List<OrderResponseDto> orders = orderService.getMyOrders(userId, type, status);
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @Operation(summary = "주문 상세 조회", description = "주문 상세 조회 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetail(@PathVariable Long orderId, @AuthenticationPrincipal Long userId) {
        OrderResponseDto response = orderService.getOrderDetail(orderId, userId);
        return ResponseEntity.ok(response);
    }

    // 주문 상태 변경
    @Operation(summary = "주문 상태 변경", description = "특정 주문의 상태를 변경하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공", // 200으로 변경 권장
                    content = @Content(schema = @Schema(implementation = OrderUpdateResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (상태 변경 권한이 없는 사용자)"), // 권한 체크 로직에 따라 추가 고려
    })
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderUpdateResponseDto> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderUpdateRequestDto dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long userId;

        try {
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new SecurityException("유효하지 않은 사용자 id");
        }

        OrderUpdateResponseDto response = orderService.updateOrderStatus(orderId, userId, dto);
        return ResponseEntity.ok(response);
    }


}
