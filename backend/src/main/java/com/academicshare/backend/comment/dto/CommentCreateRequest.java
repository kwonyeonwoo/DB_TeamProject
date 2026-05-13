package com.academicshare.backend.comment.dto;

public record CommentCreateRequest(
        String content,
        Boolean isAnonymous
) {
}
