package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInquiryRequest {
    private String title;
    private String content;
}
