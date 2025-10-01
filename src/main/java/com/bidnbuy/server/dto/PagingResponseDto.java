package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagingResponseDto<T> {
    private List<T> data;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;
}
