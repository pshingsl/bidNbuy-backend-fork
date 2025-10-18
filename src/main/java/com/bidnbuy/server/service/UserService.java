package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.UserSignupRequestDto;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.AuthStatus;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.repository.RefreshTokenRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {
    //    @Autowired
//    private UserRepository repository;
    private final UserRepository repository; //불변성, 초기화 문제 해결을 위해 @Autowired 대신 final이랑 생성자 주입으로 바꿈
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService; //회원가입 시에 이메일 인증으로 수정
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder,
                       UserRepository userRepository, EmailVerificationService emailVerificationService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";

    //회원가입 메서드 (수정)
    @Transactional
    public UserEntity signup(final UserSignupRequestDto signupRequestDto) {
        final String email = signupRequestDto.getEmail();
        final String password = signupRequestDto.getPassword();
        final String nickname = signupRequestDto.getNickname();

        if (!password.matches(PASSWORD_REGEX)) {
            log.warn("password does not meet conditions:{}", password);
            throw new RuntimeException("비밀번호는 영문과 숫자를 포함해서 8자리 이상이어야 합니다.");
        }

        //중복 이메일 검사
        if (repository.existsByEmail(email)) {
            log.warn("email already exists{}", email);
            throw new RuntimeException("email already exists");
        }

        if(!emailVerificationService.isEmailVerified(email)){
            log.warn("email not verified for signup:{}:", email );
            throw new CustomAuthenticationException("이메일 인증부터 해야 함");
        }

        UserEntity newUser = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .authStatus(AuthStatus.Y)
                .role("ROLE_USER")
                .password(passwordEncoder.encode(password))
                .build();
        return repository.save(newUser);
    }

    //로그인 검증
    public UserEntity findByEmailAndPassword(final String email, String password) {
        final Optional<UserEntity> userOptional = repository.findByEmail(email);
        final UserEntity ogUser = userOptional.orElse(null);

        //사용자 존재 여부 확인
        if (ogUser == null) {
            log.warn("User not found for email:{}", email);
            return null;
        }
        //비밀번호 일치
        if (passwordEncoder.matches(password, ogUser.getPassword())) {
            return ogUser;
        }
        log.warn("password mismatch for email:{}", email);
        return null;
    }

    public UserEntity getById(final Long id) {
        Optional<UserEntity> userOptional = repository.findById(id);
        return userOptional.orElse(null);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserEntity findOrCreateUser(String email, String nickname) {
        return repository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) //소셜로그인 비번을 채우기 위한 더미
                    .role("ROLE_USER")
                    .build();
            return repository.save(newUser);
        });
    }

    //임시비번 생성, 저장
    public String generateAndSaveTempPassword(UserEntity user) {
        String tempPassword = generateRandomPassword();
        //해시로 엔티티저장
        String hashedPassword = passwordEncoder.encode(tempPassword);
        user.setTempPasswordHash(hashedPassword);

        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);
        user.setTempPasswordExpiryDate(expiryDate);

        userRepository.save(user);

        return tempPassword;

    }

    //임시비번 랜덤문자열 생성하기
    private String generateRandomPassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    //비번 단순 유효성 검증
    private boolean isTempPasswordValid(UserEntity user, String tempPw) {
        if (user.getTempPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            return false; // 만료시간 지남
        }

        if (passwordEncoder.matches(tempPw, user.getTempPasswordHash())) {
            return true;
        }//임시비번이 맞게 입력?
        return false;
    }

    //임시비번 유효성 검증 후 업데이트 권한
    public void verifyTempPassword(String email, String tempPw) {
        UserEntity user = findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found:{}" + email));
        if (!isTempPasswordValid(user, tempPw)) {
            throw new RuntimeException("password not match");
        }
    }

    //임시비번 초기화
    private void clearTempPw(UserEntity user) {
        user.setTempPasswordHash(null);
        user.setTempPasswordExpiryDate(null);
        userRepository.save(user);
    }

    //비밀번호 업데이트
    public void updatePassword(UserEntity user, String newPassword) {
        String hashedPw = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPw);
        userRepository.save(user);
    }

    //비밀번호 최종 재설정
    public void finalResetPassword(String email, String newPassword) {
        UserEntity user = findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found:{}" + email));
        clearTempPw(user);
        updatePassword(user, newPassword);
    }

    //비밀번호 재설정(로그인한 상태에서 단순 변경)
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found:{}" + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        updatePassword(user, newPassword);
    }

    //회원 탈퇴
    public void deleteUser(Long userId, String inputPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found:{}" + userId));
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.deleteById(userId);
    }

    //userId로 찾기
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found:{} " + userId));
    }

    //로그아웃
    @Transactional
    public void logout(Long userId) {
        log.info("사용자 로그아웃{}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId + "해당 사용자를 찾을 수 없음"));

        //리프레시 토큰 연결
        refreshTokenRepository.findByUser(userEntity)
                .ifPresentOrElse(
                        refreshToken -> {
                            refreshTokenRepository.delete(refreshToken);
                            log.info("성공적으로 토큰 삭제");
                        },
                        () -> {
                            log.warn("유효한 토큰이 없음");
                        }
                );
    }

    // 프로필 이미지 조회
    public String getProfileImageUrl(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 존재하지 않습니다."));

        final String DEFAULT_PROFILE_URL = "data:image/jpeg;base64,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";

        String savedImageUrl = user.getProfileImageUrl();

        if (savedImageUrl == null || savedImageUrl.isEmpty()) {
            return DEFAULT_PROFILE_URL;
        }

        return savedImageUrl;
    }
}
