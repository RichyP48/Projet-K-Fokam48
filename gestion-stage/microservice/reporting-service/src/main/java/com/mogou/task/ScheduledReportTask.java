package com.mogou.task;

import com.mogou.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportTask {

    private final ReportGenerationService reportGenerationService;

    /**
     * S'exécute le premier jour de chaque mois à 2h du matin.
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReports() {
        try {
            log.info("Début de la génération du rapport mensuel...");
            reportGenerationService.generateAndStoreMonthlyReport();
            log.info("Rapport mensuel généré avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rapport mensuel: {}", e.getMessage());
        }
    }
}