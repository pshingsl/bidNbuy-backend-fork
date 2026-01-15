package com.bidnbuy.server.exception;

import com.bidnbuy.server.enums.ErrorCode;
import lombok.Getter;

// 커스텀
@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode exceptionCode;

    public CustomException(ErrorCode exceptionCode) {
        super(exceptionCode.getMessage()); // 이거 중요!!
        this.exceptionCode = exceptionCode;
    }
}

