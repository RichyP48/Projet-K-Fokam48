package com.mogou.listener;

import com.mogou.dto.NotificationRequestDto;
import com.mogou.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationProcessingService notificationProcessingService;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handleNotificationEvent(NotificationRequestDto request) {
        log.info("Received notification request from RabbitMQ: {}", request);
        try {
            notificationProcessingService.processAndSendNotification(request);
        } catch (Exception e) {
            log.error("Failed to process notification for user {}", request.getUserId(), e);
            // Ici, vous pourriez implémenter une logique de "dead-letter queue" pour les rejets
        }
    }
}
