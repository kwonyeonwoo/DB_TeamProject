package com.academicshare.backend.post.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostCreateRequest(
        String title,
        String content,
        String mainCategory,
        String subCategory,
        Boolean isAnonymous,
        List<MultipartFile> files
) {

    public PostCreateRequest {
        files = files == null ? List.of() : List.copyOf(files);
    }
}
