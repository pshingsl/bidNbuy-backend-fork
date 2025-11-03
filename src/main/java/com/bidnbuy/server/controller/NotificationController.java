package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminNotificationRequest;
import com.bidnbuy.server.dto.NotificationResponse;
import com.bidnbuy.server.dto.RatingRequest;
import com.bidnbuy.server.entity.NotificationEntity;
import com.bidnbuy.server.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "알림 API", description = "알림 기능 제공")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserNotificationService userNotificationService;

    // 알림 전체 읽음 처리
    @Operation(summary = "사용자 전체 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 상태로 변경 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Long userId) {
        userNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // 알림 목록 전체 조회
    @Operation(summary = "사용자 알림 목록 조회", description = "로그인한 사용자의 전체 알림 목록을 최신순으로 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public List<NotificationResponse> getUserNotifications(@AuthenticationPrincipal Long userId) {
        return userNotificationService.getUserNotifications(userId);
    }

    // 개별 알림 삭제
    @Operation(summary = "개별 알림 삭제", description = "지정된 ID의 알림을 삭제 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음") // 예외 처리 고려
    })
    @DeleteMapping("/{id}")
    public Map<String, String> deleteNotification(@PathVariable Long id) {
        userNotificationService.deleteNotification(id);
        return Map.of("message", "알림이 삭제되었습니다");
    }

    // 전체 알림 삭제
    @Operation(summary = "사용자 전체 알림 삭제", description = "로그인한 사용자의 전체 알림을 모두 삭제 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 알림 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping
    public Map<String, String> deleteAllNotifications(@AuthenticationPrincipal Long userId) {
        userNotificationService.deleteAllNotifications(userId);
        return Map.of("message", "전체 알림이 삭제되었습니다");
    }

    // userId == null 이면 전체공지, 아니면 경고 발송
    @Operation(summary = "관리자 알림 발송 (공지/경고)", description = "userId가 null이면 전체 공지, 아니면 특정 유저에게 경고 알림 발송 API") // 💡 수정됨
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 발송 성공",
                    content = @Content(schema = @Schema(type = "string", example = "전체 공지 발송 완료 또는  경고 발송 완료 (id=...)"))), // 💡 수정됨
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (관리자 권한 필요)")
    })
    @PostMapping
    public ResponseEntity<?> sendNotification(@RequestBody AdminNotificationRequest request) {
        if (request.getUserId() == null) {
            // 전체 공지
            userNotificationService.createNoticeForAll(request.getContent());
            return ResponseEntity.ok("📢 전체 공지 발송 완료");
        } else {
            // 특정 유저 경고
            NotificationEntity saved = userNotificationService.createWarning(
                    request.getUserId(),
                    request.getContent()
            );
            return ResponseEntity.ok("⚠️ 경고 발송 완료 (id=" + saved.getNotificationId() + ")");
        }
    }
}
