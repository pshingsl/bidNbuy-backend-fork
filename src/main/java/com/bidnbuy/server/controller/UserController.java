package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.RefreshTokenEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.security.JwtProvider;
import com.bidnbuy.server.service.AuthService;
import com.bidnbuy.server.service.RefreshTokenService;
import com.bidnbuy.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public UserController(UserService userService, AuthService authService){
        this.userService = userService;
        this.authService = authService;
    }

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


    //토큰 테스트를 위한 테스트 메서드
    @GetMapping("/test")
    public ResponseEntity<?> testAuth(@AuthenticationPrincipal String userId) {
        ResponseDto responseDto = ResponseDto.builder()
                .error("Authenticated! userId: " + userId)
                .build();
        return ResponseEntity.ok().body(responseDto);
    }
}
