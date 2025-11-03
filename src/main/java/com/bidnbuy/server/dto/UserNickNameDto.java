package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNickNameDto {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}
