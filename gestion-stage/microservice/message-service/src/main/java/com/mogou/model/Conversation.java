package com.mogou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long candidatureId;
    private Long etudiantId;
    private Long entrepriseId;
    private LocalDateTime lastMessageAt;
}