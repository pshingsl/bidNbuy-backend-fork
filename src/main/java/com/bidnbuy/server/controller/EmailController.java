package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.EmailRequestDto;
import com.bidnbuy.server.dto.EmailVerificationRequestDto;
import com.bidnbuy.server.service.AuthService;
import com.bidnbuy.server.service.EmailService;
import com.bidnbuy.server.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="이메일 인증 API", description = "가입 인증 시 이메일 기능")
@RequestMapping("/auth/email")
@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final AuthService authService;

    @Operation(
        summary = "이메일 인증",
        description = "회원 가입시 이메일 인증을 위한 인증 코드 발송",
        tags = {"이메일 인증 API"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이메일 발송 완료",
            content = @Content(schema = @Schema(type = "string", example = "인증 이메일이 성공적으로 발송되었습니다."))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 오류, 유효하지 않은 이메일 형식",
            content = @Content(schema = @Schema(type = "string", example = "유효하지 않은 이메일 형식"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "이메일 발송 중 서버 오류 발생",
            content = @Content(schema = @Schema(type = "string", example = "이메일 발송 중 서버 오류 발생"))

        )
    })
    //인증코드 발송
    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationEmail(@Valid @RequestBody EmailRequestDto requestDto){
        authService.sendVerificationEmail(requestDto.getEmail());

        return ResponseEntity.ok("인증 이메일이 성공적으로 발송되었습니다.");
    }


    @Operation(
            summary = "인증 코드 검증",
            description = "인증 코드 입력시 검증. 검증 성공 시 이메일 인증 완료",
            tags = {"이메일 인증 API"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "검증 완료",
            content = @Content(schema = @Schema(type = "string", example = "인증되었습니다."))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "인증 실패, 유효하지 않거나 만료된 코드",
            content = @Content(schema = @Schema(type = "string", example = "인증 실패, 유효하지 않거나 만료된 코드"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류 발생",
            content = @Content(schema = @Schema(type = "string", example = "서버 오류 발생"))
            )
    })
    //인증코드 검증
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmailCode(@Valid @RequestBody EmailVerificationRequestDto requestDto){
        authService.completeEmailVerification(requestDto.getEmail(), requestDto.getCode());
        return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 로그인해주세요.");
    }

}
