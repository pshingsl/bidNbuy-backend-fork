package com.bidnbuy.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiExceptionCode {
    INVALID_INPUT("4001", "Invalid input provided.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("4041", "Resource not found.", HttpStatus.NOT_FOUND),
    SERVER_ERROR("5001", "Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;         // 에러 코드
    private final String message;      // 에러 메시지
    private final HttpStatus httpStatus; // HTTP 상태 코드
}
