package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.security.JwtProvider;
import com.bidnbuy.server.service.AuthService;
import com.bidnbuy.server.service.EmailService;
import com.bidnbuy.server.service.ImageService;
import com.bidnbuy.server.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final ImageService imageService;

    @Autowired
    public UserController(UserService userService, AuthService authService, JwtProvider jwtProvider, EmailService emailService, ImageService imageService){
        this.userService = userService;
        this.authService = authService;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
        this.imageService = imageService;
    }

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.uri.redirect}")
    private String redirectUri;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto){
        try{
            UserEntity user  = UserEntity.builder()
                    .email(userDto.getEmail())
                    .nickname(userDto.getNickname())
                    .password(userDto.getPassword())
                    .build();
            UserEntity registeredUser = userService.create(user);

            UserDto responseUserDto = UserDto.builder()
                    .email(registeredUser.getEmail())
                    .nickname(registeredUser.getNickname())
                    .build();
            return ResponseEntity.ok().body(responseUserDto);
        }catch(Exception e){
            ResponseDto responseDto = ResponseDto.builder().error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(responseDto);
        }
    }

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

    @GetMapping("/kakao")
    public ResponseEntity<?> kakaoLogin (@RequestParam("code") String code) {
        try {
            AuthResponseDto responseDto = authService.kakaoLogin(code);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
//            log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&카카오 로그인 처리 중 에러 발생: {}", e.getMessage(), e);
            // 에러 발생 시 문자열(String)을 반환하려고 시도 (예상)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/naver/loginstart")
    public RedirectView redirectToNaver(HttpSession session){
        String state = jwtProvider.generateStateToken();
        session.setAttribute("naver_oauth_state", state);
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize" +
                                "?response_type=code" +
                                "&client_id=" + naverClientId +
                                "&redirect_uri=" + redirectUri +
                                "&state=" + state;
        return new RedirectView(naverAuthUrl);
        //http://localhost:8080/auth/naver/loginstart
    }


    @GetMapping("/naver")
    public ResponseEntity<?> naverLogin (@RequestParam("code") String code, @RequestParam("state") String state, HttpSession session) {
        String savedState = (String) session.getAttribute("naver_oauth_state");
        if (savedState == null || !savedState.equals(state)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("State token mismatch.");
        }
        session.removeAttribute("naver_oauth_state");
        try {
            AuthResponseDto responseDto = authService.naverLogin(code, state);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
//            log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&네이버 로그인 처리 중 에러 발생: {}", e.getMessage(), e);
            // 에러 발생 시 문자열(String)을 반환하려고 시도 (예상)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request){
        UserEntity user = userService.findByEmail(request.getEmail())
                .orElseThrow(()->new UsernameNotFoundException("User not found"));

        String tempPassword = userService.generateAndSaveTempPassword(user);
        emailService.sendTempPasswordEmail(user.getEmail(), tempPassword);
        return ResponseEntity.ok().build();
    }

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
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @RequestBody DeleteUserDto requestDto){
        String inputPassword = requestDto.getPassword();
        userService.deleteUser(userId, inputPassword);
        return ResponseEntity.noContent().build();
    }

    //토큰 테스트를 위한 테스트 메서드
    @GetMapping("/test")
    public ResponseEntity<?> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("@@@@@@@@@@@@@@@@@@@@2Authentication: {}", authentication);
        Long userId = (Long) authentication.getPrincipal();

        ResponseDto responseDto = ResponseDto.builder()
                .message("Authenticated! userId: " + userId)
                .build();

        return ResponseEntity.ok().body(responseDto);
    }

    // 프로필 이미지 업로드
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal Long userId,
              @RequestPart("image") MultipartFile imageFile ) {
        String newImageUrl = imageService.updateProfileImage(userId, imageFile);

        UserImageDto response = UserImageDto.builder()
                .imageUrl(newImageUrl)
                .build();

        return ResponseEntity.ok(response);
    }
}
