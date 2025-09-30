package com.mogou.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationRequestDto {
    private Long userId;
    private String templateType; // Ex: "CANDIDATURE_ACCEPTEE"
    private String channel; // "EMAIL", "WEBSOCKET", "BOTH"
    private Map<String, Object> variables;
}
