package com.bidnbuy.server.config;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemUserInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private static final String SYSTEM_EMAIL = "system@bidnbuy.com";


    @Override
    @Transactional
    public void run(String ...args) throws Exception{
        UserEntity existing = userRepository.findByEmailAndDeletedAtIsNull(SYSTEM_EMAIL);

        if(existing == null){
            //존재하지 않으면 자동생성
            log.info("시스템 유저가 존재하지 않아 새로 생성: {}", SYSTEM_EMAIL);
            UserEntity systemUser = UserEntity.builder()
                    .email(SYSTEM_EMAIL)
                    .password("$2a$10$XFSDFHIS$FALAD534WFIS")
                    .nickname("SYSTEM")
                    .role("SYSTEM")
                    .authStatus(com.bidnbuy.server.enums.AuthStatus.Y)
                    .userStatus(com.bidnbuy.server.enums.UserStatus.Y)
                    .userTemperature(0.0)
                    .penaltyPoints(0)
                    .isSuspended(false)
                    .suspensionCount(0)
                    .banCount(0)
                    .build();
            userRepository.save(systemUser);
        }else {
            log.info("존재함");
        }
    }

}
