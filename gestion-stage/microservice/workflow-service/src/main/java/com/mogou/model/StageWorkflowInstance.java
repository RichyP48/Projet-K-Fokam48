package com.mogou.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class StageWorkflowInstance {
    @Id
    private Long candidatureId; // On utilise l'ID de la candidature comme clé primaire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StageState currentState;

    private LocalDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
