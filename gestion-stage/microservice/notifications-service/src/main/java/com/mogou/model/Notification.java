package com.mogou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId; // L'ID de l'utilisateur destinataire
    @Enumerated(EnumType.STRING)
    private TypeNotification type;
    private String sujet;
    @Column(columnDefinition = "TEXT")
    private String contenu;
    @Enumerated(EnumType.STRING)
    private StatutNotification statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateVue;
}
