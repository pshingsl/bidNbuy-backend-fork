package com.bidnbuy.server.controller;

import com.bidnbuy.server.config.TossPaymentClient;
import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.PaymentEntity;
import com.bidnbuy.server.exception.PaymentErrorResponse;
import com.bidnbuy.server.service.OrderService;
import com.bidnbuy.server.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@Tag(name = "결재 API", description = "결재 기능 제공")
@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentService paymentService;
    private final OrderService orderService;

    // 결제 준비 (PENDING 저장)
    @Operation(summary = "결재 생성 ", description = "주문에 대한 결재 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 준비(PENDING) 성공", // 200으로 변경 및 설명 수정
                    content = @Content(schema = @Schema(implementation = PaymentPendingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "결제 준비 오류", // 400 명시
                    content = @Content(schema = @Schema(implementation = PaymentErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "주문 정보를 찾을 수 없음")
    })
    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveAmount(@RequestBody SaveAmountRequest request) {
        try {
            // OrderEntity 조회
            OrderEntity order = orderService.findById(request.getOrderId());

            // PaymentEntity 생성 (order FK 연결 필수)
            PaymentEntity payment = paymentService.createPendingPayment(order, request);

            log.info("✅ Payment pending saved: merchantOrderId={}, amount={}",
                    payment.getMerchantOrderId(), payment.getTotalAmount());

            return ResponseEntity.ok(new PaymentPendingResponseDto(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    PaymentErrorResponse.builder()
                            .code(400)
                            .message("결제 준비 중 오류: " + e.getMessage())
                            .build()
            );
        }
    }

    // 결제 승인 처리
    @Operation(summary = "결재 승인 생성", description = "결재 생성 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 승인 성공", // 200으로 변경
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "토스 결제 오류 (예: 금액 불일치 등)", // 외부 API 오류 명시
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버/DB 처리 오류", // 내부 오류 명시
                    content = @Content(schema = @Schema(implementation = PaymentErrorResponse.class)))
    })
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentRequestDTO request) {
        try {

            // 1) Toss 승인 요청 : paymentKey, orderId(mercharId), amount만
            HttpResponse<String> response = tossPaymentClient.requestConfirm(request);

            if (response.statusCode() != 200) {
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }

            // 2) 응답 파싱
            PaymentResponseDto dto = tossPaymentClient.parseConfirmResponse(response.body());

            log.info("✅ 승인 성공: {}",
                    dto);

            // auctionId 값 추가
            dto.setAuctionId(request.getAuctionId());

            // 3) DB 갱신
            PaymentEntity payment = paymentService.saveConfirmedPayment(dto);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    PaymentErrorResponse.builder()
                            .code(500)
                            .message("결제 승인 처리 중 오류 발생: " + e.getMessage())
                            .build()
            );
        }
    }

    //  사용자 취소 요청 (전액 취소)
    @Operation(summary = "결재 취소", description = "주문에 대한 결재 취소 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 취소 성공", // 설명 수정
                    content = @Content(schema = @Schema(implementation = PaymentCancelResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "취소 불가 상태 (이미 취소됨 등)") // 비즈니스 로직에 따라 추가 고려
    })
    @PostMapping("/cancel")
    public ResponseEntity<PaymentCancelResponseDto> cancelPayment(@RequestBody PaymentCancelRequestDto requestDto) {
        PaymentCancelResponseDto result = paymentService.cancelPayment(requestDto);
        return ResponseEntity.ok(result);
    }


}