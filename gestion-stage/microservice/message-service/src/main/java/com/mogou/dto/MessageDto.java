package com.mogou.dto;

import com.mogou.model.SenderType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private SenderType senderType;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
}