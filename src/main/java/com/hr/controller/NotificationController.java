package com.hr.controller;

import model.notification.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.notification.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMyNotifications() {
        List<Notification> notifications = notificationService.getMyNotifications();
        List<Map<String, Object>> result = notifications.stream().map(n -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", n.getId());
            map.put("message", n.getMessage());
            map.put("type", n.getType().name());
            map.put("referenceId", n.getReferenceId());
            map.put("actionUrl", n.getActionUrl());
            map.put("read", n.isRead());
            map.put("createdAt", n.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
