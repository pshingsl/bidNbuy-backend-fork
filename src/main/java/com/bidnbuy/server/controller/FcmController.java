// controller/FcmController.java
package com.bidnbuy.server.controller;

import com.bidnbuy.server.service.UserFcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "FCM API", description = "FCM 기능 제공")
@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class FcmController {

    private final UserFcmTokenService fcmTokenService;

    @Operation(summary = "사용자 FCM 토큰 등록", description = "푸시 알림 수신을 위한 사용자의 Firebase Cloud Messaging (FCM) 토큰을 서버에 등록 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FCM 토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "토큰이 누락되거나 빈 값인 경우"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/token")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, String> request, @AuthenticationPrincipal Long userId // 로그인 유저
        ) {
            log.info("🚀 [Controller] /notifications/token 진입");
            log.info("🚀 userDetails={}", userId);
            log.info("🚀 body={}", request);

        String fcmToken = request.get("token");
        if (fcmToken == null || fcmToken.isBlank()) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        fcmTokenService.registerToken(userId, fcmToken);
        return ResponseEntity.ok("Token registered");
    }
}
