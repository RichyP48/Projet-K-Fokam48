package com.mogou.controller;

import com.mogou.dto.MessageDto;
import com.mogou.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageService messageService;

    @GetMapping("/conversation/{candidatureId}")
    public ResponseEntity<List<MessageDto>> getConversationHistory(@PathVariable Long candidatureId) {
        List<MessageDto> history = messageService.getConversationHistory(candidatureId);
        return ResponseEntity.ok(history);
    }

    // TODO: Implémenter les endpoints pour marquer comme lu et compter les messages non lus
    /*
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        // ...
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        // ...
        return ResponseEntity.ok(count);
    }
    */
}
