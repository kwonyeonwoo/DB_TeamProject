package com.academicshare.backend.post.dto;

import com.academicshare.backend.post.domain.PostFile;

public record FileResponse(
        Integer id,
        String fileUrl,
        String fileName,
        String contentType
) {

    public static FileResponse from(PostFile file) {
        return new FileResponse(
                file.getId(),
                file.getFileUrl(),
                file.getFileName(),
                file.getContentType()
        );
    }
}
