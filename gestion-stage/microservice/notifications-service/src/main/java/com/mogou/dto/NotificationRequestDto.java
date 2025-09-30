package com.mogou.dto;

import com.mogou.model.TypeNotification;
import lombok.Data;

import java.util.Map;

/**
 * Objet reçu via RabbitMQ pour déclencher une notification.
 */
@Data
public class NotificationRequestDto {
    private Long userId;
    private String userEmail;
    private String templateType;
    private TypeNotification channel;
    private Map<String, Object> variables;
}