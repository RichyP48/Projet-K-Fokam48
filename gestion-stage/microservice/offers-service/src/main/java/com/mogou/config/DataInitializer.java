package com.mogou.config;

import com.mogou.model.OffreStage;
import com.mogou.repository.OffreStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OffreStageRepository offreStageRepository;

    @Override
    public void run(String... args) throws Exception {
        if (offreStageRepository.count() == 0) {
            OffreStage offre = new OffreStage();
            offre.setTitre("Stage Développement Web");
            offre.setDescription("Développement d'applications web avec Spring Boot et Angular");
            offre.setEntrepriseId(4L);
            offre.setDuree(6);
            offre.setSalaire(800.0);
            offre.setLocalisation("Paris");
            offre.setCompetencesRequises("Java, Spring Boot, Angular");
            
            OffreStage offre2 = new OffreStage();
            offre2.setTitre("Stage Marketing Digital");
            offre2.setDescription("Gestion des campagnes marketing et réseaux sociaux");
            offre2.setEntrepriseId(4L);
            offre2.setDuree(4);
            offre2.setSalaire(600.0);
            offre2.setLocalisation("Lyon");
            offre2.setCompetencesRequises("Marketing, Communication");
            
            offreStageRepository.save(offre2);
            offreStageRepository.save(offre);
            log.info("Offres de test créées");
        }
    }
}