package com.academicshare.backend.notification.dto;

import com.academicshare.backend.notification.domain.Notification;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NotificationResponse(
        Integer id,
        Boolean isRead,
        String commentContent,
        Integer commentedPostId,
        Integer commentedUserId,
        Integer commentedId,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getIsRead(),
                notification.getCommentContent(),
                notification.getCommentedPostId(),
                notification.getCommentedUserId(),
                notification.getCommentedId(),
                notification.getCreatedAt()
        );
    }
}
