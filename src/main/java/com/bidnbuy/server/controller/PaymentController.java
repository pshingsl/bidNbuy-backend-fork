package com.bidnbuy.server.controller;

import com.bidnbuy.server.config.TossPaymentClient;
import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.PaymentEntity;
import com.bidnbuy.server.exception.PaymentErrorResponse;
import com.bidnbuy.server.service.OrderService;
import com.bidnbuy.server.service.PaymentService;
import com.sun.tools.jconsole.JConsoleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 결제 준비 (PENDING 저장)
     */
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

    /**
     * 결제 승인 처리
     */
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
    @PostMapping("/cancel")
    public ResponseEntity<PaymentCancelResponseDto> cancelPayment(@RequestBody PaymentCancelRequestDto requestDto) {
        PaymentCancelResponseDto result = paymentService.cancelPayment(requestDto);
        return ResponseEntity.ok(result);
    }


}