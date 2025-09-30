package com.mogou.client;

import com.mogou.dto.NotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationClient {
    // Cet endpoint doit exister dans le notifications-service
    @PostMapping("/api/notifications/send-event")
    void sendNotification(@RequestBody NotificationRequestDto request);
}
