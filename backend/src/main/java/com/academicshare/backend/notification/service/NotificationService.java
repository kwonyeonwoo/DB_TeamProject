package com.academicshare.backend.notification.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.notification.domain.Notification;
import com.academicshare.backend.notification.dto.NotificationResponse;
import com.academicshare.backend.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    public NotificationService(NotificationRepository notificationRepository, CurrentUserProvider currentUserProvider) {
        this.notificationRepository = notificationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public List<NotificationResponse> getCurrentUserNotifications() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByCommentedUserIdOrderByCreatedAtDesc(currentUserId);

        notifications.forEach(Notification::markAsRead);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public Optional<Notification> createCommentNotificationIfNeeded(
            Integer postAuthorId,
            Integer commentAuthorId,
            Integer postId,
            String commentContent
    ) {
        if (Objects.equals(postAuthorId, commentAuthorId)) {
            return Optional.empty();
        }

        return Optional.of(notificationRepository.save(new Notification(
                commentContent,
                postId,
                postAuthorId,
                null
        )));
    }

    @Transactional
    public Optional<Notification> createReplyNotificationIfNeeded(
            Integer parentCommentAuthorId,
            Integer replyAuthorId,
            Integer postId,
            Integer parentCommentId,
            String replyContent
    ) {
        if (Objects.equals(parentCommentAuthorId, replyAuthorId)) {
            return Optional.empty();
        }

        return Optional.of(notificationRepository.save(new Notification(
                replyContent,
                postId,
                parentCommentAuthorId,
                parentCommentId
        )));
    }
}
