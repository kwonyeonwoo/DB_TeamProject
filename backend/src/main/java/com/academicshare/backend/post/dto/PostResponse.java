package com.academicshare.backend.post.dto;

import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.domain.PostFile;
import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Integer id,
        Integer userId,
        String authorDisplayName,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer viewCount,
        String mainCategory,
        String subCategory,
        Boolean isAnonymous,
        List<FileResponse> files,
        Boolean likedByMe,
        Long likeCount
) {

    public static PostResponse from(
            Post post,
            String authorDisplayName,
            List<PostFile> files,
            boolean likedByMe,
            long likeCount
    ) {
        return new PostResponse(
                post.getId(),
                post.getUserId(),
                authorDisplayName,
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                post.getMainCategory(),
                post.getSubCategory(),
                post.getIsAnonymous(),
                files.stream().map(FileResponse::from).toList(),
                likedByMe,
                likeCount
        );
    }
}
