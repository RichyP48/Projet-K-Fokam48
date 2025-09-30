package com.mogou.service;

import com.mogou.dto.MessageDto;

import java.util.List;

public interface MessageService {
    MessageDto sendMessage(Long candidatureId, MessageDto messageDto, String username);
    List<MessageDto> getConversationHistory(Long candidatureId);
}
