package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.security.JwtProvider;
import com.bidnbuy.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final JwtProvider tokenProvider;

    @Autowired
    public UserController(UserService userService, JwtProvider tokenProvider){
        this.userService = userService;
        this.tokenProvider = tokenProvider;
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
        UserEntity loginUser = userService.findByEmailAndPassword(
                userDto.getEmail(),
                userDto.getPassword()
        );
        //로그인 성공/실패 응답 처리
        if(loginUser != null){
            //로그인 성공 시 토큰 생성
            String accessToken = tokenProvider.createAccessToken(loginUser.getUserId());

            TokenResponseDto tokenInfo = TokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(null)
                    .grantType(tokenProvider.getGrantType())
                    .accessTokenExpiresIn(tokenProvider.getAccessTokenExpirationTime())
                    .build();
            AuthResponseDto reponseDto = AuthResponseDto.builder()
                    .email(loginUser.getEmail())
                    .nickname(loginUser.getNickname())
                    .tokenInfo(tokenInfo)
                    .build();
            return ResponseEntity.ok().body(reponseDto);
        }else{
            ResponseDto responseDto = ResponseDto.builder()
                    .error("Login failed. Check your email and password.")
                    .build();
            return ResponseEntity.status(401).body(responseDto);
        }
    }
}
