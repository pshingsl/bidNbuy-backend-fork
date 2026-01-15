package com.bidnbuy.server.exception;

import com.bidnbuy.server.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RestApiException extends RuntimeException {
    private final ErrorCode errorCode;
}
