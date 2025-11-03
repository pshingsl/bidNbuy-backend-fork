package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.PenaltyRequestDto;
import com.bidnbuy.server.enums.PenaltyType;
import com.bidnbuy.server.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 페널티 API", description = "관리자 페널티 관련 기능 제공")
@RestController
@RequestMapping("/admin/penalty")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class PenaltyController {
    
    private final PenaltyService penaltyService;

    @Operation(summary = "페널티 부과", description = "페널티 부과", tags = {"관리자 페널티 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "페널티 부과 성공",
            content = @Content(schema = @Schema(type = "string", example = "페널티가 성공적으로 부과되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류 (사용자 없음, 잘못된 페널티 타입)",
            content = @Content(schema = @Schema(type = "string", example = "사용자를 찾을 수 없습니다."))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<?> applyPenalty(@RequestBody PenaltyRequestDto request) {
        log.info("관리자 페널티 부과 요청: userId={}, type={}", request.getUserId(), request.getType());
        
        try {
            PenaltyType penaltyType = PenaltyType.valueOf(request.getType());
            penaltyService.applyPenalty(request.getUserId(), penaltyType);
            
            return ResponseEntity.ok().body("페널티가 성공적으로 부과되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("페널티 부과 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("페널티 부과 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("페널티 부과 중 오류가 발생했습니다.");
        }
    }
}