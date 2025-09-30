package com.mogou.statemachine;

import com.mogou.client.ApplicationClient;
import com.mogou.client.ConventionClient;
import com.mogou.dto.CandidatureStatusDto;
import com.mogou.model.StageEvent;
import com.mogou.model.StageState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StateMachineActions {

    private final ApplicationClient applicationClient;
    private final ConventionClient conventionClient;

    /**
     * GARDE : Vérifie si la candidature est bien acceptée avant de générer la convention.
     */
    @Bean
    public Guard<StageState, StageEvent> checkCandidatureAcceptedGuard() {
        return context -> {
            Long candidatureId = (Long) context.getMessageHeader("candidatureId");
            log.info("Garde : Vérification du statut pour la candidature ID {}", candidatureId);
            if (candidatureId == null) return false;

            CandidatureStatusDto statusDto = applicationClient.getCandidatureStatus(candidatureId);
            boolean isAccepted = "ACCEPTEE".equals(statusDto.getStatut());
            if (!isAccepted) {
                log.warn("Transition bloquée : la candidature {} n'a pas le statut ACCEPTEE.", candidatureId);
            }
            return isAccepted;
        };
    }

    /**
     * ACTION : Appelle le conventions-service pour générer le PDF de la convention.
     */
    @Bean
    public Action<StageState, StageEvent> genererConventionAction() {
        return context -> {
            Long candidatureId = (Long) context.getMessageHeader("candidatureId");
            log.info("Action : Déclenchement de la génération de convention pour la candidature ID {}", candidatureId);
            if (candidatureId != null) {
                conventionClient.generateConvention(Map.of("candidatureId", candidatureId));
                log.info("Appel à conventions-service effectué.");
            }
        };
    }
}
