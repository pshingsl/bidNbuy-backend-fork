package com.bidnbuy.server.enums;

import lombok.Getter;

@Getter
public class paymentStatus {

    public enum PaymentMethod {
        VIRTUAL_ACCOUNT, SIMPLE_PAY, GAME_GIFT, TRANSFER, BOOK_GIFT, CULTURE_GIFT, CARD, MOBILE;

        // 프론트(한글) -> 백(영어) 변환 로직
        public static PaymentMethod fromToss(String method) {
            if (method == null) return null;
            return switch (method) {
                case "카드" -> CARD;
                case "간편결제" -> SIMPLE_PAY;
                case "가상계좌" -> VIRTUAL_ACCOUNT;
                case "휴대폰" -> MOBILE;
                case "게임문화상품권" -> GAME_GIFT;
                case "도서문화상품권" -> BOOK_GIFT;
                case "문화상품권" -> CULTURE_GIFT;
                case "계좌이체" -> TRANSFER;
                default -> null;
            };
        }
    }

    public enum PaymentStatus {
        SUCCESS, FAIL, CANCEL, REFUND, PENDING
    }

}
