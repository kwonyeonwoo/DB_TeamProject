package com.academicshare.backend.common.response;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalCount,
        int totalPages
) {

    public PageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
