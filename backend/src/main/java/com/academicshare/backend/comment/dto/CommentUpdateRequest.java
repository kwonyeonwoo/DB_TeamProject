package com.academicshare.backend.comment.dto;

public record CommentUpdateRequest(
        String content,
        boolean contentProvided,
        Boolean isAnonymous,
        boolean isAnonymousProvided
) {

    public boolean hasAnyField() {
        return contentProvided || isAnonymousProvided;
    }
}
