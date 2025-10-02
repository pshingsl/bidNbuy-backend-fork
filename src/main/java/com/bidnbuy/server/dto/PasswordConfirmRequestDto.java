package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordConfirmRequestDto {
    private String email;
    private String tempPassword;
    private String newPassword;
}
