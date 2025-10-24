// controller/FcmController.java
package com.bidnbuy.server.controller;

import com.bidnbuy.server.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class FcmController {

    private final UserFcmTokenService fcmTokenService;

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
