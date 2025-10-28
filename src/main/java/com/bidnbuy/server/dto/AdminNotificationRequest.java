package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNotificationRequest  {
    private Long userId; // 공지면 null, 경고면 값 있음
    private String content;
}