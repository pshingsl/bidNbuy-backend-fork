package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.EmailVerificationEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.AuthStatus;
import com.bidnbuy.server.enums.IsVerified;
import com.bidnbuy.server.repository.EmailVerificationRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.Temporal;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveVerificationCode(String email, String code){
        Instant expiryTime = Instant.now().plus(5, ChronoUnit.MINUTES); //5분
        //인증기록 찾기
        EmailVerificationEntity verificationEntity = emailVerificationRepository.findByEmail(email)
                .orElseGet(EmailVerificationEntity::new);
        //업데이트
        verificationEntity.setEmail(email);
        verificationEntity.setValidCode(code);
        verificationEntity.setExpirationTime(expiryTime);
        verificationEntity.setIsVerified(IsVerified.N); // 코드 새로 발급, 인증상태 초기화하기
        //디비 저장
        emailVerificationRepository.save(verificationEntity);
    }

    @Transactional
    public void verifyCode(String email, String inputCode){
        //db인증기록 조회
        EmailVerificationEntity verificationEntity = emailVerificationRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("인증 정보가 없습니다."));
        //만료시간 검증
        if(Instant.now().isAfter(verificationEntity.getExpirationTime())){
            throw new RuntimeException("인증 코드가 만료되었습니다. 재전송해주세요.");
        }
        //코드일치여부
        if(!verificationEntity.getValidCode().equals(inputCode)){
            throw new RuntimeException("인증 코드가 일치하지 않습니다.");
        }
        //검증 성공시 auth_status값 업데이트
        verificationEntity.setIsVerified(IsVerified.Y); //Transactional으로 자동 저장됨!
//        updateUserAuthStatus(email);
    }

    public boolean isEmailVerified(String email){
        return emailVerificationRepository.findByEmail(email)
                .map(entity->entity.getIsVerified() == IsVerified.Y)
                .orElse(false);
    }

    @Transactional
    public void clearVerificationStatus(String email){
        emailVerificationRepository.findByEmail(email)
                .ifPresent(entity->{
                    entity.setIsVerified(IsVerified.N);
                });
    }

}
