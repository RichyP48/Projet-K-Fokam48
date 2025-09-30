package com.mogou.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequestDto {
    private Long recipientId;
    private String subject;
    private String message;
}
