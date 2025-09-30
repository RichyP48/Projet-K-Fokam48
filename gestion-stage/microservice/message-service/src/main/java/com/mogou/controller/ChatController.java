package com.mogou.controller;

import com.mogou.dto.MessageDto;
import com.mogou.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat/{candidatureId}/send")
    public void sendMessage(
            @DestinationVariable Long candidatureId,
            @Payload MessageDto messageDto,
            Principal principal) {

        // principal.getName() contient le username extrait du JWT
        MessageDto savedMessage = messageService.sendMessage(candidatureId, messageDto, principal.getName());

        // Diffuse le message à tous les abonnés du canal de la conversation
        messagingTemplate.convertAndSend("/topic/conversation/" + candidatureId, savedMessage);
    }
}