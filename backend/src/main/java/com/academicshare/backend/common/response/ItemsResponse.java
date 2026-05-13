package com.academicshare.backend.common.response;

import java.util.List;

public record ItemsResponse<T>(
        List<T> items
) {

    public ItemsResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
