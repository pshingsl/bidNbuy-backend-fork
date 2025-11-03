package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminLoginRequestDto;
import com.bidnbuy.server.dto.AdminSignupRequestDto;
import com.bidnbuy.server.dto.PasswordConfirmRequestDto;
import com.bidnbuy.server.dto.PasswordRequestDto;
import com.bidnbuy.server.dto.PasswordResetRequestDto;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.dto.TokenReissueRequestDto;
import com.bidnbuy.server.dto.TokenResponseDto;
import com.bidnbuy.server.dto.UpdateIpRequestDto;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.service.AdminAuthService;
import com.bidnbuy.server.service.EmailService;
import com.bidnbuy.server.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 인증 API", description = "관리자 인증 관련 기능 제공")
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final UserService userService;
    private final EmailService emailService;

    @Operation(summary = "관리자 회원가입", description = "관리자 회원가입, IP 수집 동의 필요", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공 및 관리자 계정 생성",
            content = @Content(schema = @Schema(implementation = AdminEntity.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 데이터(이메일 중복, 유효성 검사 실패, IP 수집 미동의)",
            content = @Content(schema = @Schema(type = "string", example = "이미 존재하는 이메일입니다.")))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AdminSignupRequestDto requestDto, HttpServletRequest request) {
        log.info("관리자 회원가입 요청: {}", requestDto.getEmail());
        
        try {
            AdminEntity savedAdmin = adminAuthService.signup(requestDto, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
        } catch (Exception e) {
            log.error("관리자 회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "관리자 로그인", description = "관리자 로그인, IP 화이트리스트 검증", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공, 인증 정보 반환, 토큰 발급",
            content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "인증 실패(이메일, 비밀번호 불일치 또는 허용되지 않은 IP)",
            content = @Content(schema = @Schema(type = "string", example = "허용되지 않은 IP에서의 접근입니다.")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequestDto requestDto, HttpServletRequest request) {
        log.info("관리자 로그인 요청: {}", requestDto.getEmail());
        
        try {
            TokenResponseDto tokenResponse = adminAuthService.login(requestDto, request);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("관리자 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "관리자 IP 주소 업데이트", description = "관리자 IP 주소 업데이트", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "IP 주소 업데이트 성공",
            content = @Content(schema = @Schema(type = "string", example = "IP 주소가 성공적으로 업데이트되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류(IP 형식 오류, 요청 IP와 새 IP 불일치)",
            content = @Content(schema = @Schema(type = "string", example = "요청 IP와 새 IP가 일치하지 않습니다."))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/ip")
    public ResponseEntity<?> updateAllowedIp(@Valid @RequestBody UpdateIpRequestDto updateIpDto, HttpServletRequest request) {
        log.info("관리자 IP 업데이트 요청: {}", updateIpDto.getNewIpAddress());
        
        try {
            adminAuthService.updateAllowedIp(updateIpDto, request);
            return ResponseEntity.ok().body("IP 주소가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("관리자 IP 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "관리자 토큰 재발급", description = "관리자 토큰 재발급", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패(유효하지 않거나 만료된 토큰)",
            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@Valid @RequestBody TokenReissueRequestDto requestDto) {
        log.info("관리자 토큰 재발급 요청");
        
        try {
            TokenResponseDto reissueResponse = adminAuthService.reissueAdminToken(requestDto.getRefreshToken());
            return ResponseEntity.ok().body(reissueResponse);
        } catch (CustomAuthenticationException e) {
            log.error("관리자 토큰 재발급 실패: {}", e.getMessage());
            ResponseDto<String> responseDto = ResponseDto.<String>builder().error(e.getMessage()).build();
            return ResponseEntity.status(401).body(responseDto);
        } catch (Exception e) {
            log.error("관리자 토큰 재발급 실패: {}", e.getMessage());
            ResponseDto<String> responseDto = ResponseDto.<String>builder().error("Internal Server Error during token reissue.").build();
            return ResponseEntity.status(500).body(responseDto);
        }
    }

    @Operation(summary = "관리자 로그아웃", description = "관리자 로그아웃(refresh token 삭제)", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(Authentication authentication) {
        log.info("관리자 로그아웃 요청");
        
        try {
            Object principal = authentication.getPrincipal();
            Long adminId;
            
            if (principal instanceof Long) {
                adminId = (Long) principal;
            } else {
                return ResponseEntity.status(401).build();
            }
            
            userService.logoutAdmin(adminId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("관리자 로그아웃 중 에러 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "관리자 임시 비밀번호 요청", description = "관리자 임시 비밀번호 요청(이메일 발송)", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임시 비밀번호 발송 성공",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호가 이메일로 발송되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류(관리자를 찾을 수 없음)",
            content = @Content(schema = @Schema(type = "string", example = "관리자를 찾을 수 없습니다."))),
        @ApiResponse(responseCode = "500", description = "임시 비밀번호 생성 실패 또는 이메일 발송 오류",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호 생성 실패, 이메일 발송 오류")))
    })
    @PostMapping("/password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        log.info("관리자 임시 비밀번호 요청: {}", request.getEmail());
        
        try {
            AdminEntity admin = adminAuthService.findByEmail(request.getEmail());
            String tempPassword = adminAuthService.generateAndSaveTempPassword(admin);
            emailService.sendTempPasswordEmailForAdmin(admin.getEmail(), tempPassword);
            
            return ResponseEntity.ok().body("임시 비밀번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            log.error("관리자 임시 비밀번호 요청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "관리자 임시 비밀번호 검증", description = "관리자 임시 비밀번호 검증", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임시 비밀번호 검증 성공",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호가 확인되었습니다. 새 비밀번호를 설정해 주세요."))),
        @ApiResponse(responseCode = "400", description = "임시 비밀번호 불일치 또는 만료 또는 관리자를 찾을 수 없음",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호가 일치하지 않거나 만료되었습니다.")))
    })
    @PostMapping("/password/verify")
    public ResponseEntity<?> confirmPasswordUpdate(@RequestBody PasswordConfirmRequestDto requestDto) {
        log.info("관리자 임시 비밀번호 검증 요청: {}", requestDto.getEmail());
        
        try {
            adminAuthService.verifyTempPassword(requestDto.getEmail(), requestDto.getTempPassword());
            return ResponseEntity.ok().body("임시 비밀번호가 확인되었습니다. 새 비밀번호를 설정해 주세요.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("해당 관리자를 찾을 수 없습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("임시 비밀번호가 일치하지 않거나 만료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.");
        }
    }

    @Operation(summary = "관리자 비밀번호 재설정", description = "관리자 비밀번호 재설정", tags = {"관리자 인증 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공",
            content = @Content(schema = @Schema(type = "string", example = "새 비밀번호가 성공적으로 설정되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류 또는 기타 비밀번호 조건 불일치",
            content = @Content(schema = @Schema(type = "string", example = "비밀번호는 영문과 숫자를 포함해 8자리 이상이어야 합니다.")))
    })
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordRequestDto requestDto) {
        log.info("관리자 새 비밀번호 설정: {}", requestDto.getEmail());
        
        try {
            adminAuthService.finalResetPassword(requestDto.getEmail(), requestDto.getNewPassword());
            return ResponseEntity.ok().body("새 비밀번호가 성공적으로 설정되었습니다.");
        } catch (Exception e) {
            log.error("관리자 새 비밀번호 설정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}