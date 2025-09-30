package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notifications-service")
public interface NotificationClient {

    // L'endpoint doit correspondre à celui du service de notifications
    @PostMapping("/api/notifications")
    void sendNotification(Object notificationPayload); // Remplacez Object par votre DTO de notification
}