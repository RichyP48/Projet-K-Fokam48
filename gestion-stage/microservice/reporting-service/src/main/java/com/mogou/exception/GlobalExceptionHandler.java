package com.mogou.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException e) {
        String message = "Erreur de communication avec un service externe. Service distant indisponible ou requête invalide.";
        if (e.status() == 404) {
            message = "Une des ressources demandées dans un autre service n'a pas été trouvée.";
        }
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    // Ajoutez d'autres handlers pour les exceptions spécifiques au reporting

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
        return new ResponseEntity<>(body, status);
    }
}