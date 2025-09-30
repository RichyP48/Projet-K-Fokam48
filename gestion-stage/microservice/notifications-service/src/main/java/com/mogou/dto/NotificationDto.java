package com.mogou.dto;

import com.mogou.model.StatutNotification;
import com.mogou.model.TypeNotification;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Objet retourné par l'API REST pour afficher l'historique des notifications.
 */
@Data
public class NotificationDto {
    private Long id;
    private TypeNotification type;
    private String sujet;
    private String contenu;
    private StatutNotification statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateVue;
}
