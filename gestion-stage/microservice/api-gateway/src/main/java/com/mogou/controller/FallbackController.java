package com.mogou.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, String>>> userServiceFallback() {
        return buildFallbackResponse("Service Utilisateur Indisponible",
                "Le service de gestion des utilisateurs est actuellement indisponible. Veuillez réessayer plus tard.");
    }

    @GetMapping("/offers-service")
    public Mono<ResponseEntity<Map<String, String>>> offersServiceFallback() {
        return buildFallbackResponse("Service Offres Indisponible",
                "Le service de gestion des offres est temporairement en panne. Réessayez ultérieurement.");
    }

    @GetMapping("/applications-service")
    public Mono<ResponseEntity<Map<String, String>>> applicationsServiceFallback() {
        return buildFallbackResponse("Service Candidatures Indisponible",
                "Impossible d’accéder aux candidatures pour le moment. Merci de patienter.");
    }

    @GetMapping("/conventions-service")
    public Mono<ResponseEntity<Map<String, String>>> conventionsServiceFallback() {
        return buildFallbackResponse("Service Conventions Indisponible",
                "Le service de gestion des conventions est indisponible. Réessayez plus tard.");
    }

    @GetMapping("/evaluation-service")
    public Mono<ResponseEntity<Map<String, String>>> evaluationServiceFallback() {
        return buildFallbackResponse("Service Évaluations Indisponible",
                "Le service d’évaluation rencontre un problème. Veuillez réessayer.");
    }

    @GetMapping("/messaging-service")
    public Mono<ResponseEntity<Map<String, String>>> messagingServiceFallback() {
        return buildFallbackResponse("Service Messagerie Indisponible",
                "La messagerie est actuellement hors service. Réessayez ultérieurement.");
    }

    @GetMapping("/notifications-service")
    public Mono<ResponseEntity<Map<String, String>>> notificationsServiceFallback() {
        return buildFallbackResponse("Service Notifications Indisponible",
                "Impossible d’envoyer ou de recevoir des notifications pour le moment.");
    }

    @GetMapping("/reporting-service")
    public Mono<ResponseEntity<Map<String, String>>> reportingServiceFallback() {
        return buildFallbackResponse("Service Reporting Indisponible",
                "Le service de reporting est en maintenance. Merci de réessayer plus tard.");
    }

    @GetMapping("/workflow-service")
    public Mono<ResponseEntity<Map<String, String>>> workflowServiceFallback() {
        return buildFallbackResponse("Service Workflow Indisponible",
                "Le moteur de workflow est temporairement indisponible.");
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testGateway() {
        Map<String, Object> response = Map.of(
            "message", "API Gateway is working!",
            "timestamp", System.currentTimeMillis(),
            "gateway", "api-gateway"
        );
        return ResponseEntity.ok(response);
    }

    // ==========================
    // Méthode utilitaire DRY
    // ==========================
    private Mono<ResponseEntity<Map<String, String>>> buildFallbackResponse(String error, String message) {
        Map<String, String> response = Map.of(
                "error", error,
                "message", message
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}