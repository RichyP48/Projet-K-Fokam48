package com.mogou.service;


import com.mogou.dto.StatistiqueFiliereDto;
import com.mogou.model.RapportPeriodique;
import com.mogou.model.TypeRapport;
import com.mogou.repository.RapportPeriodiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final StatisticsService statisticsService;
    private final RapportPeriodiqueRepository rapportRepository;

    @org.springframework.transaction.annotation.Transactional
    public void generateAndStoreMonthlyReport() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        String periode = previousMonth.toString(); // Ex: "2024-09"

        // 1. Calculer les statistiques pour le mois précédent
        List<StatistiqueFiliereDto> stats = statisticsService.getStatistiquesParFiliere(periode);

        // 2. Créer et sauvegarder l'entité RapportPeriodique
        RapportPeriodique rapport = new RapportPeriodique();
        rapport.setType(TypeRapport.MENSUEL);
        rapport.setPeriode(periode);
        rapport.setDateGeneration(LocalDate.now());
        // Stocke les données calculées dans le champ JSONB
        rapport.setDonnees(Map.of("statistiquesFiliere", stats));

        rapportRepository.save(rapport);
    }
}
