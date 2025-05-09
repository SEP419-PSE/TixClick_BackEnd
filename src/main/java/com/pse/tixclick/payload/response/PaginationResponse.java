package com.pse.tixclick.payload.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationResponse<T> {
    private List<T> items;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
