package com.academicshare.backend.comment.dto;

import com.academicshare.backend.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CommentResponse(
        Integer id,
        Integer userId,
        String authorDisplayName,
        Integer postId,
        Integer parentComment,
        String content,
        Boolean isAnonymous,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentResponse from(Comment comment, String authorDisplayName) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                authorDisplayName,
                comment.getPostId(),
                comment.getParentComment(),
                comment.getContent(),
                comment.getIsAnonymous(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
