package com.mogou.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configure le serveur WebSocket et STOMP.
 * C'est ici que l'on définit les points d'entrée pour la connexion
 * et les "topics" pour la diffusion des messages.
 */
@Configuration
@EnableWebSocketMessageBroker // Active le broker de messages WebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Notre intercepteur personnalisé pour la sécurité JWT
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Définit les préfixes des destinations où les messages seront routés par le broker.
        // - "/topic": Pour les messages de groupe (broadcast).
        // - "/user": Pour les messages privés, Spring le résout automatiquement vers une session utilisateur spécifique.
        registry.enableSimpleBroker("/topic", "/user");

        // Définit le préfixe pour les messages envoyés depuis les clients vers le serveur.
        // Par exemple, un client enverra un message à "/app/notify".
        registry.setApplicationDestinationPrefixes("/app");

        // Configure le préfixe pour les destinations spécifiques à un utilisateur.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Définit le point d'entrée pour la connexion WebSocket.
        // Les clients se connecteront à cette URL (ex: "http://localhost:8085/ws/notifications").
        // - setAllowedOriginPatterns("*"): Autorise les connexions de n'importe quelle origine (à ajuster en production).
        // - withSockJS(): Fournit une alternative (fallback) pour les navigateurs qui ne supportent pas les WebSockets natifs.
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // C'est l'étape cruciale pour la sécurité.
        // On attache notre intercepteur au canal d'entrée des messages clients.
        // Il interceptera chaque connexion pour valider le token JWT avant d'établir la session.
        registration.interceptors(authChannelInterceptor);
    }
}

