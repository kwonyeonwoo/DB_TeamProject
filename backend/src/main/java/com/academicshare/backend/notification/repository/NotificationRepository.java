package com.academicshare.backend.notification.repository;

import com.academicshare.backend.notification.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByCommentedUserIdOrderByCreatedAtDesc(Integer commentedUserId);
}
