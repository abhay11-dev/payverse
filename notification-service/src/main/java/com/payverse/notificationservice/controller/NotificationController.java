package com.payverse.notificationservice.controller;

import com.payverse.notificationservice.model.Notification;
import com.payverse.notificationservice.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public Page<Notification> getNotifications(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @PatchMapping("/{id}/read")
public Notification markAsRead(
        @PathVariable Long id,
        @RequestParam Long userId
) {
    Notification notification = notificationRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Notification not found"
                    )
            );

    notification.setRead(true);

    return notificationRepository.save(notification);
}


@GetMapping("/unread-count")
public long getUnreadCount(
        @RequestParam Long userId
) {
    return notificationRepository.countByUserIdAndIsReadFalse(userId);
}
}
