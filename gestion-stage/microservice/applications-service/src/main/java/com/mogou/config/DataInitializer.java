package com.mogou.config;

import com.mogou.enums.StatutCandidature;
import com.mogou.model.Candidature;
import com.mogou.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CandidatureRepository candidatureRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeCandidatures();
    }

    private void initializeCandidatures() {
        if (candidatureRepository.count() == 0) {
            // Candidature pour l'étudiant Johan (ID: 2)
            Candidature candidature1 = Candidature.builder()
                .etudiantId(2L)
                .offreId(1L)
                .entrepriseId(4L) // Research Center PKF
                .statut(StatutCandidature.POSTULE)
                .datePostulation(LocalDateTime.now().minusDays(5))
                .commentaires("Candidature test pour stage développement")
                .build();
            
            Candidature candidature2 = Candidature.builder()
                .etudiantId(2L)
                .offreId(2L)
                .entrepriseId(4L) // Research Center PKF
                .statut(StatutCandidature.EN_ATTENTE)
                .datePostulation(LocalDateTime.now().minusDays(3))
                .commentaires("Candidature test pour stage marketing")
                .build();
            
            candidatureRepository.save(candidature1);
            candidatureRepository.save(candidature2);
            
            log.info("Candidatures de test initialisées");
        }
    }
}