package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequestDto {
    private String title;
    private String content;
}