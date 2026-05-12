package com.academicshare.backend.notification.controller;

import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.notification.dto.NotificationResponse;
import com.academicshare.backend.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ItemsResponse<NotificationResponse> getNotifications() {
        return new ItemsResponse<>(notificationService.getCurrentUserNotifications());
    }
}
