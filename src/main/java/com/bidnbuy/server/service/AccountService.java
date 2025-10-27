package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.BankAccountRequest;
import com.bidnbuy.server.dto.BankAccountResponse;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepository userRepository;

    @Transactional
    public void registerBankAccount(Long userId, BankAccountRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request.getAccountNumber() == null || request.getAccountNumber().isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }

        user.setBankName(request.getBankName());
        user.setAccountNumber(request.getAccountNumber());
        user.setAccountHolder(request.getAccountHolder());

        userRepository.save(user);
    }

    @Transactional
    public BankAccountResponse getBankAccount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return new BankAccountResponse(
                user.getBankName(),
                user.getAccountNumber(),
                user.getAccountHolder()
        );
    }
}
