package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.BankAccountRequest;
import com.bidnbuy.server.dto.BankAccountResponse;
import com.bidnbuy.server.dto.RatingRequest;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import com.bidnbuy.server.service.AccountService;
import com.bidnbuy.server.service.UserService;
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

import java.util.Map;

@Tag(name = "계좌 API", description = "계좌 기능 제공")
@RestController
@RequestMapping("/bank-account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "사용자 계좌 등록", description = "사용자 계좌를 등록하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 등록 완료 메시지",
                    content = @Content(schema = @Schema(type = "string", example = "계좌 등록 완료"))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<?> registerBankAccount(@AuthenticationPrincipal Long userId, @RequestBody BankAccountRequest request) {
        accountService.registerBankAccount(userId, request);
        return ResponseEntity.ok("계좌 등록 완료");
    }

    @Operation(summary = "사용자 계좌 조회", description = "사용자 계좌 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BankAccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "등록된 계좌 정보를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<BankAccountResponse> getBankAccount(@AuthenticationPrincipal Long userId) {
        BankAccountResponse response = accountService.getBankAccount(userId);
        return ResponseEntity.ok(response);
    }

}
