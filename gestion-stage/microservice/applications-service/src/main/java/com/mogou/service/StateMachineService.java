package com.mogou.service;

import com.mogou.enums.StatutCandidature;
import com.mogou.model.Candidature;
import com.mogou.model.HistoriqueCandidature;
import com.mogou.repository.HistoriqueRepository;
import com.mogou.statemachine.CandidatureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StateMachineService {

    public static final String CANDIDATURE_HEADER = "candidature";

    private final StateMachineFactory<StatutCandidature, CandidatureEvent> stateMachineFactory;
    private final HistoriqueRepository historiqueRepository;
    // private final NotificationClient notificationClient; // Pour les notifications

    @Transactional
    public Candidature sendEvent(Candidature candidature, CandidatureEvent event, String commentaire, Long userId) {
        StateMachine<StatutCandidature, CandidatureEvent> sm = build(candidature);

        Message<CandidatureEvent> message = MessageBuilder.withPayload(event)
                .setHeader(CANDIDATURE_HEADER, candidature)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();

        // Mettre à jour le statut et créer l'historique
        StatutCandidature ancienStatut = candidature.getStatut();
        StatutCandidature nouveauStatut = sm.getState().getId();

        candidature.setStatut(nouveauStatut);
        createHistory(candidature, ancienStatut, nouveauStatut, commentaire, userId);

        // Envoyer une notification
        // notificationClient.sendNotification(...);

        return candidature;
    }

    public void createInitialHistory(Candidature candidature) {
        createHistory(candidature, null, StatutCandidature.POSTULE, "Candidature créée.", candidature.getEtudiantId());
    }

    private void createHistory(Candidature candidature, StatutCandidature ancien, StatutCandidature nouveau, String comm, Long userId) {
        HistoriqueCandidature historique = HistoriqueCandidature.builder()
                .candidature(candidature)
                .ancienStatut(ancien)
                .nouveauStatut(nouveau)
                .commentaire(comm)
                .dateChangement(LocalDateTime.now())
                .userId(userId)
                .build();
        historiqueRepository.save(historique);
    }

    private StateMachine<StatutCandidature, CandidatureEvent> build(Candidature candidature) {
        StateMachine<StatutCandidature, CandidatureEvent> sm = stateMachineFactory.getStateMachine(Long.toString(candidature.getId()));
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> sma.resetStateMachine(
                        new DefaultStateMachineContext<>(candidature.getStatut(), null, null, null)
                ));
        sm.start();
        return sm;
    }
}

