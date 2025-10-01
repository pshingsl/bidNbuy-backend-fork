package com.bidnbuy.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NaverTokenResponseDto {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh")
    private String refreshToken;

    @JsonProperty("tokenType")
    private String token_type;

    @JsonProperty("expires_in")
    private String expiresIn;

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}
