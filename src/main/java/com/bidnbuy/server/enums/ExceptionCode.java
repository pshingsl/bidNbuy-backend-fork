package com.bidnbuy.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    // 200 OK : 성공
    SUCCESS(OK, "", 200),

    // 400 BAD_REQUEST : 잘못된 요청 - 비즈니스 로직 에러코드를 여기에 작성해주세요!
    AUCTION_NOT_FOUND(NOT_FOUND, "존재하지 않는 경매 물품입니다", 404),
    SELF_BIDDING_FORBIDDEN(FORBIDDEN, "자신의 경매 물품에는 입찰할 수 없습니다", 403),
    AUCTION_NOT_IN_PROGRESS(BAD_REQUEST, "경매가 진행 중이 아닙니다", 400),
    EXPIRED_AUCTION_TIME(BAD_REQUEST, "경매 시간이 종료되었습니다", 400),
    INVALID_MIN_BID(BAD_REQUEST, "최소 입찰 단위를 충족해야 합니다", 400),
    LOWER_THAN_CURRENT_PRICE(BAD_REQUEST, "이미 더 높은 입찰가가 존재합니다", 400),

    // 401 UNAUTHORIZED : 인증되지 않은 사용자
    INVALID_TOKEN(UNAUTHORIZED, "잘못된 토큰입니다", 401),
    INVALID_EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다", 401),
    INVALID_DELETED_MEMBER(UNAUTHORIZED, "탈퇴한 회원입니다", 401),

    // 403 Forbidden : 자원에 대한 권한 없음
    INVALID_AUTH(FORBIDDEN, "권한이 없습니다", 403),

    // 404 Not Found : 요청한 URI에 대한 리소스 없음
    INVALID_RESOURCE(NOT_FOUND, "요청한 리소스가 없습니다", 404),
    INVALID_USER_ID(NOT_FOUND, "존재하지 않는 사용자 입니다", 404),

    // 405 Method Not Allowed : 사용 불가능한 Method 이용
    INVALID_METHOD(METHOD_NOT_ALLOWED, "지원하지 않는 Method 입니다.", 405),

    // 500 INTERNAL_SERVER_ERROR : 서버 에러
    SERVER_ERROR(INTERNAL_SERVER_ERROR,"", 500);

    private final HttpStatus httpStatus;
    private final String message;
    private final int code;
}
