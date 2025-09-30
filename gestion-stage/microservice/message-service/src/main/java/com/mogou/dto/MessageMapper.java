package com.mogou.dto;

import com.mogou.model.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageMapper {

    public MessageDto toDto(Message message) {
        if (message == null) return null;
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderType(message.getSenderType());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setRead(message.isRead());
        return dto;
    }

    public Message toEntity(MessageDto dto) {
        if (dto == null) return null;
        Message message = new Message();
        message.setConversationId(dto.getConversationId());
        message.setSenderId(dto.getSenderId());
        message.setSenderType(dto.getSenderType());
        message.setContent(dto.getContent());
        message.setSentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now());
        message.setRead(dto.isRead());
        return message;
    }
}
