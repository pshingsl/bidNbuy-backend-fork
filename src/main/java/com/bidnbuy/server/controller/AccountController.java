package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.BankAccountRequest;
import com.bidnbuy.server.dto.BankAccountResponse;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import com.bidnbuy.server.service.AccountService;
import com.bidnbuy.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank-account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> registerBankAccount(@AuthenticationPrincipal Long userId, @RequestBody BankAccountRequest request) {
        accountService.registerBankAccount(userId, request);
        return ResponseEntity.ok("계좌 등록 완료");
    }

    @GetMapping
    public ResponseEntity<BankAccountResponse> getBankAccount(@AuthenticationPrincipal Long userId) {
        BankAccountResponse response = accountService.getBankAccount(userId);
        return ResponseEntity.ok(response);
    }

}
