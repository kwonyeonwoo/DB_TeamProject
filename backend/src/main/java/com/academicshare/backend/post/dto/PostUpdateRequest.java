package com.academicshare.backend.post.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostUpdateRequest(
        String title,
        boolean titleProvided,
        String content,
        boolean contentProvided,
        String mainCategory,
        boolean mainCategoryProvided,
        String subCategory,
        boolean subCategoryProvided,
        Boolean isAnonymous,
        boolean isAnonymousProvided,
        List<MultipartFile> files
) {

    public PostUpdateRequest {
        files = files == null ? List.of() : List.copyOf(files);
    }

    public boolean hasUploadedFiles() {
        return files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    public boolean hasAnyField() {
        return titleProvided
                || contentProvided
                || mainCategoryProvided
                || subCategoryProvided
                || isAnonymousProvided
                || hasUploadedFiles();
    }
}
