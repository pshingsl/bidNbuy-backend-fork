package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.TradeFilterStatus;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.security.JwtProvider;
import com.bidnbuy.server.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "유저 API", description = "유저 기능 제공")
@Slf4j
@RestController
@RequestMapping({"/auth", "/api/auth"})
public class   UserController {

    @Value("${front.redirect.uri}")
    private String frontUri;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.uri}")
    private String redirectUrl;

    private final UserService userService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final ImageService imageService;
    private final AuctionResultService auctionResultService;

    @Autowired
    public UserController(UserService userService, AuthService authService, JwtProvider jwtProvider, EmailService emailService, ImageService imageService, AuctionResultService auctionResultService){
        this.userService = userService;
        this.authService = authService;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
        this.imageService = imageService;
        this.auctionResultService = auctionResultService;
    }

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${kakao.client.id}")
    private String kakaoClientId;


    @Value("${naver.uri.redirect}")
    private String redirectUri;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;


    @Operation(summary = "회원가입", description = "회원가입", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공 및 사용자 생성",
            content = @Content(schema = @Schema(implementation = UserEntity.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터(중복 이메일, 유효성 검사 실패)",
            content = @Content(schema = @Schema(type = "string", example = "이미 존재하는 이메일입니다.")))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequestDto requestDto){
        log.info("회원가입 요청 DTO: {}", requestDto);
        try{
            UserEntity savedUser = userService.signup(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }catch (RuntimeException e){
            log.error("회원가입 실패:{}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "로그인", description = "로그인 후 토큰 발급", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공, 인증 정보 반환, 토큰 발급",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패(이메일, 비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = ResponseDto.class, example ="로그인 실패")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto){
        try {
            AuthResponseDto responseDto = authService.login(userDto.getEmail(), userDto.getPassword());
            return ResponseEntity.ok().body(responseDto);
        } catch (Exception e) {
            ResponseDto responseDto = ResponseDto.builder()
                    .error("Login failed. Check your email and password.")
                    .build();
            return ResponseEntity.status(401).body(responseDto);
        }
    }

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token으로 재발급", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토크 재발급 성공",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패(유효하지 않거나 만료된 Refresh Token 토큰)",
            content = @Content(schema = @Schema(implementation = ResponseDto.class, example = "유효하지 않거나 만료된 Refresh Token 토큰"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생",
            content = @Content(schema = @Schema(implementation = ResponseDto.class, example = "서버 내부 오류 발생")))
    })
    //토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestBody TokenReissueRequestDto requestDto){
        try {
            AuthResponseDto reissueResponse = authService.reissue(requestDto.getRefreshToken());

            return ResponseEntity.ok().body(reissueResponse);

        } catch (CustomAuthenticationException e) {
            // CustomAuthenticationException 처리
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();
            return ResponseEntity.status(401).body(responseDto);
        } catch (Exception e) {
            // 기타 예상치 못한 오류 (500)
            ResponseDto responseDto = ResponseDto.builder().error("Internal Server Error during token reissue.").build();
            return ResponseEntity.status(500).body(responseDto);
        }

    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인증 후 리다이렉트 엔드포인트" , tags = {"유저 API"}, hidden = true)
    @Parameter(name = "code", description = "카카오로에서 받은 인증 코드", required = true)
    @GetMapping("/kakao")
    public void kakaoLogin (@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            AuthResponseDto auth = authService.kakaoLogin(code);

            String accessToken = auth.getAccessToken();
            String refreshToken = auth.getRefreshToken();

            String redirectUrl = frontUri + "/oauth/callback" +
                    "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                    "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            String errorRedirectUrl = frontUri + "/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirectUrl);
        }
    }

    @GetMapping("/kakao/loginstart")
    public RedirectView redirectToKakao() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUri +
                "&response_type=code";
        log.info("카카오 로그인 리다이렉트: {}", kakaoAuthUrl);
        return new RedirectView(kakaoAuthUrl);
    }


    @Operation(summary = "네이버 로그인", description = "네이버 인증 후 리다이렉트" , tags = {"유저 API"})
    @GetMapping("/naver/loginstart")
    public RedirectView redirectToNaver(HttpSession session){
        String state = jwtProvider.generateStateToken();
        session.setAttribute("naver_oauth_state", state);

        log.info("### NAVER redirectUri = {}", redirectUri);

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + naverClientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;
        return new RedirectView(naverAuthUrl);
        //http://localhost:8080/auth/naver/loginstart
    }

    @Operation(summary = "네이버 로그인 콜백", description = "네이버 인증 후 리다이렉트 엔드포인트" , tags = {"유저 API"}, hidden = true)
    @Parameter(name = "code", description = "네이버에서 받은 인증 코드", required = true)
    @Parameter(name = "state", description = "세션에 저장된 상태 토큰", required = true)
    @GetMapping("/naver")
    public void naverLogin (@RequestParam("code") String code, @RequestParam("state") String state,
                            HttpSession session, HttpServletResponse response) throws IOException{
        String savedState = (String) session.getAttribute("naver_oauth_state");

        if (savedState == null || !savedState.equals(state)){
            log.warn("Naver login failed: State token mismatch.");
            String errorRedirectUrl = frontUri + "/login?error=" + URLEncoder.encode("State token mismatch.", StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirectUrl);
            return;
        }
        try {
            AuthResponseDto authResponse = authService.naverLogin(code, state);

            String accessToken = authResponse.getAccessToken();
            String refreshToken = authResponse.getRefreshToken();

            String redirectUrl = frontUri + "/oauth/callback" +
                    "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                    "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 에러 발생: {}", e.getMessage(), e);
            String errorRedirectUrl = frontUri + "/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirectUrl);
        }
    }

    @Operation(summary = "임시 비밀번호 요청", description = "이메일로 임시 비밀번호 발급, 발송 요청", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임시 비밀번호 발송 성공"),
        @ApiResponse(responseCode = "404",description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(type = "string", example = "사용자를 찾을 수 없음"))),
        @ApiResponse(responseCode = "500",description = "임시 비밀번호 생성 실패, 이메일 발송 오류",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호 생성 실패, 이메일 발송 오류"))),
    })
    @PostMapping("/password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request){
        UserEntity user = userService.findByEmail(request.getEmail())
                .orElseThrow(()->new UsernameNotFoundException("User not found"));

        String tempPassword = userService.generateAndSaveTempPassword(user);
        emailService.sendTempPasswordEmail(user.getEmail(), tempPassword);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "임시 비밀번호 확인", description = "사용자 입력 임시 비밀번호 유효한지 검증", tags = {"유저API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임시 비밀번호 확인 성공",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호 확인. 새 비밀번호를 설정하세요"))),
        @ApiResponse(responseCode = "400", description = "임시 비밀번호 불일치 또는 만료",
            content = @Content(schema = @Schema(type = "string", example = "임시 비밀번호 불일치 또는 만료"))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(type = "string", example = "사용자를 찾을 수 없음")))
    })
    //임시비밀번호 검증
    @PostMapping("/password/verify")
    public ResponseEntity<?> confirmPasswordUpdate(@RequestBody PasswordConfirmRequestDto requestDto){
        try{
            userService.verifyTempPassword(
                    requestDto.getEmail(),
                    requestDto.getTempPassword()
            );
            return ResponseEntity.ok().body("임시 비밀번호가 확인되었습니다. 새 비번을 설정하세요.");
        }catch (UsernameNotFoundException e){
            return ResponseEntity.badRequest().body("해당 유저를 찾을 수 없습니다.");
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("임시 비밀번호 시간이 만료되었거나, 일치하지 않습니다.");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("알 수 없는 오류발생 다시 시도해주세요.");
        }
    }

    @Operation(summary = "비밀번호 재설정", description = "임시 비밀번호 확인 후 새 비밀번호로 최종 변경합니다.", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",description = "비밀번호 재설정 성공",
            content = @Content(schema = @Schema(type = "string", example = "새 비밀번호 성공적으로 설정 완료")) ),
        @ApiResponse(responseCode = "400",description = "요청 오류 또는 재설정 조건 불일치",
            content = @Content(schema = @Schema(type = "string", example = "알 수 없는 오류발생 다시 시도해주세요.")))
    })
    //찐 비밀번호 재설정
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordRequestDto requestDto){
        try{
            userService.finalResetPassword(
                    requestDto.getEmail(),
                    requestDto.getNewPassword()
            );
            return ResponseEntity.ok().body("새 비밀번호 성공적으로 설정 완료");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("알 수 없는 오류발생 다시 시도해주세요.");
        }
    }


    @Operation(summary = "마이페이지 비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.", tags = {"유저 API"})
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",description = "비밀번호 변경 성공",
            content = @Content(schema = @Schema(type = "string", example = "비밀번호가 성공적으로 변경되었습니다."))),
        @ApiResponse(responseCode = "401",description = "인증 실패 (현재 비밀번호 불일치)"),
        @ApiResponse(responseCode = "404",description = "사용자 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(type = "string", example = "사용자 정보를 찾을 수 없습니다."))),
        @ApiResponse(responseCode = "500",description = "서버 오류")
    })
    //마이페이지에서 비밀번호 재설정
    @PostMapping("/user/password/change")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDto requestDto){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(authentication.getName());

            userService.changePassword(
                    userId,
                    requestDto.getCurrentPassword(),
                    requestDto.getNewPassword()
            );
            return ResponseEntity.ok().body("비밀번호가 성공적으로 변경되었습니다.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    //회원탈퇴
    @Operation(summary = "회원 탈퇴", description = "사용자 ID와 비밀번호를 확인하여 계정을 삭제합니다.", tags = {"유저 API"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401",description = "인증 실패 (비밀번호 불일치)"),
            @ApiResponse(responseCode = "404",description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @RequestBody DeleteUserDto requestDto){
        String inputPassword = requestDto.getPassword();
        userService.deleteUser(userId, inputPassword);
        return ResponseEntity.noContent().build();
    }

    // 프로필 이미지 업로드
    @Operation(summary = "유저프로필 이미지 수정 API", description = "유저프로필 이미지 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserImageDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal Long userId,
                                                @RequestPart("images") MultipartFile imageFile ) {
        String newImageUrl = imageService.updateProfileImage(userId, imageFile);

        UserImageDto response = UserImageDto.builder()
                .profileImageUrl(newImageUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    // 프로필 이미지 조회
    @Operation(summary = "유저프로필 이미지 조회 API", description = "유저프로필 이미지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserImageDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfileImage(@AuthenticationPrincipal Long userId) {

        String profileImage = userService.getProfileImageUrl(userId);

        UserImageDto response = UserImageDto.builder()
                .profileImageUrl(profileImage)
                .build();

        return ResponseEntity.ok(response);
    }

    // 닉네임 조회
    @Operation(summary = "유저 닉네임 조회 API", description = "유저 닉네임 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserNickNameDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{userId}/nickname")
    public ResponseEntity<?> geteNickName(@AuthenticationPrincipal Long userId) {

        String nickname = userService.getNickName(userId);

        UserNickNameDto response = UserNickNameDto.builder()
                .nickname(nickname)
                .build();
        return  ResponseEntity.ok(response);
    }

    // 닉네임 업데이트
    @Operation(summary = "유저 닉네임 수정 API", description = "유저 닉네임 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserNickNameDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{userId}/nickname")

    public ResponseEntity<?> updateNickname(@AuthenticationPrincipal Long userId, @RequestBody UserNickNameDto dto) {

        String nickname = userService.updateNickName(userId, dto.getNickname());

        UserNickNameDto response = UserNickNameDto.builder()
                .nickname(nickname)
                .build();

        return  ResponseEntity.ok(response);
    }

    // 다른 유저 프로필 조회
    @Operation(summary = "다른 유저 프로필 조회 API", description = "다른 유저 프로필 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/other/{targetId}")
    public ResponseEntity<UserProfileSummaryDto> getUserProfile(@AuthenticationPrincipal Long userId, @PathVariable Long targetId) {

        // AuctionResultService를 사용하여 다른 사용자의 프로필 요약 정보를 가져옵니다.
        UserProfileSummaryDto profile = userService.getOtherUserProfile(userId, targetId);

        return ResponseEntity.ok(profile);
    }

    // 다른 유저 프로필 구매내역 확인
    @Operation(summary = "다른 프로필 유저 구매내역 조회 API", description = "다른 유저 프로필 구매내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/users/{userId}/purchases")
    public ResponseEntity<List<AuctionSalesHistoryDto>> getUserPurchaseHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AuctionSalesHistoryDto> history = auctionResultService.getPurchaseHistoryByUser(userId, page, size);
        return ResponseEntity.ok(history);
    }


    //로그아웃
    @Operation(summary = "로그아웃", description = "사용자의 인증 정보(세션/토큰)를 무효화하고 로그아웃 처리")
    @ApiResponses(value = {
            // 실제 코드가 200 OK, Body 없음이므로 200으로 변경하고 content를 생략합니다.
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),

            // 401 응답은 인증 정보가 잘못되었거나 없는 경우를 나타냅니다.
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않거나 누락된 토큰)",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class, example = "유효하지 않은 인증 정보입니다.")))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication){
        Object principal = authentication.getPrincipal();
        Long currentUserId;

        if(principal instanceof Long){
            currentUserId = (Long) principal;
        }else{
            return  ResponseEntity.status(401).build();
        }

        userService.logout(currentUserId);

        return ResponseEntity.ok().build();
    }
}