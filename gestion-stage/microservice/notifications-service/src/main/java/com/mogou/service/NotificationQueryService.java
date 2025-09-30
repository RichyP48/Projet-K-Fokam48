package com.mogou.service;

import com.mogou.dto.NotificationDto;

import java.util.List;

public interface NotificationQueryService {
    /**
     * Récupère toutes les notifications pour un utilisateur donné, triées par date.
     * @param userId L'ID de l'utilisateur.
     * @return Une liste de DTOs de notification.
     */
    List<NotificationDto> getNotificationsForUser(Long userId);

    /**
     * Marque une notification spécifique comme lue.
     * @param notificationId L'ID de la notification à marquer.
     */
    void markAsRead(Long notificationId);
}