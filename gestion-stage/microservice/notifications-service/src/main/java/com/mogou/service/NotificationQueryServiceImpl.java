package com.mogou.service;


import com.mogou.dto.NotificationDto;
import com.mogou.model.Notification;
import com.mogou.model.StatutNotification;
import com.mogou.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        // TODO: Ajouter une vérification de sécurité pour s'assurer que l'utilisateur connecté est bien le propriétaire de la notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée")); // Remplacer par une exception personnalisée

        if (notification.getStatut() != StatutNotification.VUE) {
            notification.setStatut(StatutNotification.VUE);
            notification.setDateVue(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setSujet(notification.getSujet());
        dto.setContenu(notification.getContenu());
        dto.setStatut(notification.getStatut());
        dto.setDateCreation(notification.getDateCreation());
        dto.setDateVue(notification.getDateVue());
        return dto;
    }
}
