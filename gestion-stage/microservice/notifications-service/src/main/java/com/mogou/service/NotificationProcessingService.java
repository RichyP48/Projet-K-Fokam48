package com.mogou.service;


import com.mogou.dto.NotificationRequestDto;
import com.mogou.model.Notification;
import com.mogou.model.StatutNotification;
import com.mogou.model.TemplateNotification;
import com.mogou.model.TypeNotification;
import com.mogou.repository.NotificationRepository;
import com.mogou.repository.TemplateRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessingService {

    private final NotificationRepository notificationRepository;
    private final TemplateRepository templateRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate; // Pour WebSocket
    private final SpringTemplateEngine thymeleafTemplateEngine;

    @org.springframework.transaction.annotation.Transactional
    public void processAndSendNotification(NotificationRequestDto request) {
        TemplateNotification template = templateRepository.findByType(request.getTemplateType())
                .orElseThrow(() -> new RuntimeException("Template non trouvé pour le type : " + request.getTemplateType()));

        // Préparation du contenu avec Thymeleaf
        Map<String, Object> variables = request.getVariables();
        Context thymeleafContext = new Context(Locale.FRENCH, variables);

        String processedHtmlContent = thymeleafTemplateEngine.process(template.getContenuHtml(), thymeleafContext);

        // Sauvegarde de la notification en base de données
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getChannel());
        notification.setSujet(template.getSujet());
        notification.setContenu(processedHtmlContent);
        notification.setStatut(StatutNotification.EN_ATTENTE);
        notification.setDateCreation(LocalDateTime.now());
        notificationRepository.save(notification);

        // Envoi via les canaux demandés
        boolean emailSent = false;
        boolean wsSent = false;

        if (request.getChannel() == TypeNotification.EMAIL || request.getChannel() == TypeNotification.BOTH) {
            try {
                emailService.sendHtmlEmail(request.getUserEmail(), template.getSujet(), processedHtmlContent);
                emailSent = true;
            } catch (MessagingException e) {
                log.error("Échec de l'envoi de l'email pour la notification {}", notification.getId(), e);
                notification.setStatut(StatutNotification.ERREUR);
            }
        }

        if (request.getChannel() == TypeNotification.WEBSOCKET || request.getChannel() == TypeNotification.BOTH) {
            // Le "topic" est spécifique à l'utilisateur
            messagingTemplate.convertAndSendToUser(request.getUserId().toString(), "/topic/notifications", notification);
            wsSent = true;
        }

        // Mise à jour finale du statut
        if (emailSent || wsSent) {
            notification.setStatut(StatutNotification.ENVOYE);
            notification.setDateEnvoi(LocalDateTime.now());
        }
        notificationRepository.save(notification);
    }
}
