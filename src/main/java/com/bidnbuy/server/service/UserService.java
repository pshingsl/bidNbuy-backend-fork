package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
//    @Autowired
//    private UserRepository repository;
    private final UserRepository repository; //불변성, 초기화 문제 해결을 위해 @Autowired 대신 final이랑 생성자 주입으로 바꿈
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
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
        final UserEntity ogUser = repository.findByEmail(email);
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


}
