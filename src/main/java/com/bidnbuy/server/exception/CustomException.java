package com.bidnbuy.server.exception;

import com.bidnbuy.server.enums.ExceptionCode;
import lombok.Getter;

// 커스텀
@Getter
public class CustomException extends RuntimeException {
  private final ExceptionCode exceptionCode;

    public CustomException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage()); // 이거 중요!!
        this.exceptionCode = exceptionCode;
    }
}

