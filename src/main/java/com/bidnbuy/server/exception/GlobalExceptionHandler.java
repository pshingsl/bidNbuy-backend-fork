package com.bidnbuy.server.exception;

import com.bidnbuy.server.enums.ExceptionCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException ex) {
        ExceptionCode code = ex.getExceptionCode();

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(Map.of(
                        "code", code.getCode(),
                        "message", code.getMessage()
                ));
    }
}