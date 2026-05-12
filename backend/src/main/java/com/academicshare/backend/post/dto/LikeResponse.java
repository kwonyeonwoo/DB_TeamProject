package com.academicshare.backend.post.dto;

import com.academicshare.backend.post.domain.Like;
import java.time.LocalDateTime;

public record LikeResponse(
        Integer id,
        Integer userId,
        Integer postId,
        LocalDateTime createdAt
) {

    public static LikeResponse from(Like like) {
        return new LikeResponse(
                like.getId(),
                like.getUserId(),
                like.getPostId(),
                like.getCreatedAt()
        );
    }
}
