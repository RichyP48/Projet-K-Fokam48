package com.mogou.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mogou.dto.NotificationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // Pour convertir l'objet en JSON

    @Value("${app.rabbitmq.exchange-name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Point d'entrée pour la communication inter-services.
     * Reçoit une demande de notification et la publie dans RabbitMQ pour un traitement asynchrone.
     */
    @PostMapping("/send-event")
    public ResponseEntity<Void> receiveNotificationRequest(@Valid @RequestBody NotificationRequestDto request) {
        log.info("Requête de notification reçue pour l'utilisateur ID {}. Publication dans RabbitMQ...", request.getUserId());
        try {
            // Convertir l'objet DTO en une chaîne JSON
            String message = objectMapper.writeValueAsString(request);
            // Envoyer le message à l'exchange RabbitMQ avec la clé de routage
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);

            // On retourne un statut 202 Accepted pour signifier que la requête a été acceptée
            // pour traitement, mais n'est pas encore terminée.
            return ResponseEntity.accepted().build();

        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la sérialisation de la requête de notification en JSON", e);
            return ResponseEntity.status(500).build();
        }
    }
}
