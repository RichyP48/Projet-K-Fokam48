package com.mogou.tasks;

import com.mogou.client.NotificationClient;
import com.mogou.dto.NotificationRequestDto;
import com.mogou.model.StageState;
import com.mogou.model.StageWorkflowInstance;
import com.mogou.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final WorkflowInstanceRepository workflowRepository;
    private final NotificationClient notificationClient;
    // Injectez ici les autres clients si nécessaire pour obtenir les IDs des utilisateurs à notifier
    // private final ConventionClient conventionClient;

    /**
     * S'exécute tous les jours à 9h du matin.
     * Cherche les conventions qui ont été générées mais pas validées par l'enseignant
     * depuis plus de 3 jours et envoie un rappel.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkForStalledWorkflows() {
        log.info("Vérification des workflows en attente de validation...");

        // 1. Définir la date limite (maintenant - 3 jours)
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // 2. Trouver les instances bloquées en appelant notre nouvelle méthode
        List<StageWorkflowInstance> stalledInstances = workflowRepository.findStalledInstances(
                StageState.CONVENTION_VALIDEE_ENSEIGNANT, // L'état à surveiller
                threeDaysAgo
        );

        if (stalledInstances.isEmpty()) {
            log.info("Aucun workflow bloqué trouvé. Fin de la tâche.");
            return;
        }

        log.info("{} workflow(s) bloqué(s) trouvé(s). Envoi des rappels...", stalledInstances.size());

        // 3. Pour chaque instance bloquée, envoyer une notification
        for (StageWorkflowInstance instance : stalledInstances) {
            try {
                // TODO: Logique pour trouver l'ID de l'enseignant à notifier.
                // Vous devrez probablement appeler le conventions-service avec instance.getCandidatureId()
                // pour obtenir l'enseignantId.
                Long enseignantIdToNotify = 123L; // ID de l'enseignant en placeholder

                log.info("Envoi d'un rappel pour la candidature {} à l'enseignant ID {}",
                        instance.getCandidatureId(), enseignantIdToNotify);

                // Construire la requête de notification
                NotificationRequestDto reminder = NotificationRequestDto.builder()
                        .userId(enseignantIdToNotify)
                        .templateType("RAPPEL_VALIDATION_CONVENTION")
                        .channel("EMAIL")
                        .variables(Map.of("candidatureId", instance.getCandidatureId()))
                        .build();

                // Appeler le service de notification
                notificationClient.sendNotification(reminder);

            } catch (Exception e) {
                log.error("Échec de l'envoi d'un rappel pour la candidature {}", instance.getCandidatureId(), e);
            }
        }
        log.info("Envoi des rappels terminé.");
    }

}
