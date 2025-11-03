package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.RatingRequest;
import com.bidnbuy.server.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "정산 API", description = "정산 기능 제공")
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    // 구매자가 거래 완료를 확정할 때 호출 => (정산금 전달: 미구현)
    @Operation(summary = "거래 확정 및 정산 요청", description = "구매자가 거래 완료를 확정하여 정산 처리를 요청하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정산 성공",
                    // 실제 반환되는 문자열 메시지를 명확히 표현
                    content = @Content(schema = @Schema(type = "string", example = "정산 완료 처리되었습니다."))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "주문 정보를 찾을 수 없음")
    })
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmSettlement(@PathVariable Long orderId, @AuthenticationPrincipal Long userId) {
        settlementService.confirmSettlement(orderId, userId);
        return ResponseEntity.ok("정산 완료 처리되었습니다.");
    }
}
