package com.mogou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long conversationId;
    private Long senderId;
    @Enumerated(EnumType.STRING)
    private SenderType senderType;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead = false;
}
