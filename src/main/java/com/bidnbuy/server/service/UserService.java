package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
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

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";

    //회원가입
    public UserEntity create(final UserEntity userEntity){
        //유효성 검사 - 이메일, 비밀번호  >>누락<<
        if(userEntity == null || userEntity.getEmail() == null || userEntity.getPassword() == null){
            throw new RuntimeException("Invalid argument");
        }

        //비밀번호 유효성 검사
        final String password = userEntity.getPassword();
        if(!password.matches(PASSWORD_REGEX)){
            log.warn("password does not meet conditions:{}", password);
            throw new RuntimeException("비밀번호는 영문과 숫자를 포함해서 8자리 이상이어야 합니다.");
        }

        //중복 이메일 검사
        final String email = userEntity.getEmail();
        if(repository.existsByEmail(email)){
            log.warn("email already exists{}", email);
            throw new RuntimeException("email already exists");
        }
        //비밀번호 암호화
        final String ogPw = userEntity.getPassword();
        final String encodedPw = passwordEncoder.encode(ogPw);
        userEntity.setPassword(encodedPw);

        return repository.save(userEntity);
    }

    //로그인 검증
    public UserEntity findByEmailAndPassword(final String email, String password){
        final Optional<UserEntity> userOptional  = repository.findByEmail(email);
        final UserEntity ogUser = userOptional.orElse(null);

        //사용자 존재 여부 확인
        if(ogUser == null){
            log.warn("User not found for email:{}", email);
            return null;
        }
        //비밀번호 일치
        if(passwordEncoder.matches(password, ogUser.getPassword())){
            return ogUser;
        }
        log.warn("password mismatch for email:{}", email);
        return null;
    }

    public UserEntity getById(final Long id){
        Optional<UserEntity> userOptional = repository.findById(id);
        return userOptional.orElse(null);
    }

    public Optional<UserEntity> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserEntity findOrCreateUser(String email, String nickname){
        return repository.findByEmail(email).orElseGet(()->{
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
    public String generateAndSaveTempPassword(UserEntity user){
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
    private String generateRandomPassword(){
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);

        for(int i=0; i<8; i++){
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    //비밀번호 업데이트
    public void updatePassword(UserEntity user, String newPw){
        String hashedPw = passwordEncoder.encode(newPw);
        user.setPassword(hashedPw);
        userRepository.save(user);
    }

    //비번 재설정 확인, 업데이트
    public void verifyAndSetNewPassword(String email, String tempPw, String newPw){
        UserEntity user = findByEmail(email).orElseThrow(()->new UsernameNotFoundException("user not found" +email));
        //임시 비밀번호 유효성, 만료 시간 검사하기
        if(!isTempPasswordValid(user, tempPw)){
            throw new RuntimeException(("timeout"));
        }
        clearTempPw(user);
        updatePassword(user, newPw);
    }

    //비번 유효성 검증
    private boolean isTempPasswordValid(UserEntity user, String tempPw){
        if(user.getTempPasswordExpiryDate().isBefore(LocalDateTime.now())){
            return false; // 만료시간 지남
        }

        if(passwordEncoder.matches(tempPw, user.getTempPasswordHash())){
            return true;
        }//임시비번이 맞게 입력?
        return false;
    }

    //임시비번 초기화
    private void clearTempPw(UserEntity user){
        user.setTempPasswordHash(null);
        user.setTempPasswordExpiryDate(null);
        userRepository.save(user);
    }
}
