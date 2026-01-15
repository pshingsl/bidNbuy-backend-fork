package com.bidnbuy.server.enums;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int statusCode;
    private final String error;
    private final String message;

    public ErrorResponse(ErrorCode exceptionCode) {
        this.statusCode = exceptionCode.getHttpStatus().value();
        this.error = exceptionCode.getHttpStatus().name();
        this.message = exceptionCode.getMessage();
    }
}
