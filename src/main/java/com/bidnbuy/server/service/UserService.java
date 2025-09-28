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

    //회원가입
    public UserEntity create(final UserEntity userEntity){
        //유효성 검사 - 이메일, 비밀번호  >>누락<<
        if(userEntity == null || userEntity.getEmail() == null || userEntity.getPassword() == null){
            throw new RuntimeException("Invalid argument");
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


}
