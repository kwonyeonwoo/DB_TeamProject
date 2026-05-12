package com.academicshare.backend.post.dto;

public record PostListQuery(
        int page,
        int size,
        String keyword,
        String author,
        String mainCategory,
        String subCategory
) {
}
