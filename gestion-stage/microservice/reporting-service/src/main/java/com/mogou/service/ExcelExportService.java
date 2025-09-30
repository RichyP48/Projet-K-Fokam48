package com.mogou.service;


import com.mogou.dto.StatistiqueFiliereDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final StatisticsService statisticsService;

    public byte[] exportStatistiquesFiliereToExcel(String periode) throws IOException {
        List<StatistiqueFiliereDto> stats = statisticsService.getStatistiquesParFiliere(periode);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Statistiques par Filière");

            // Créer l'en-tête (header)
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Filière", "Période", "Nombre d'Étudiants", "Stages Obtenus", "Taux de Placement (%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // Optionnel : Ajouter un peu de style à l'en-tête
                 CellStyle headerStyle = workbook.createCellStyle();
                 Font font = workbook.createFont();
                 font.setBold(true);
                 headerStyle.setFont(font);
                 cell.setCellStyle(headerStyle);
            }

            // Remplir les lignes de données
            int rowNum = 1;
            for (StatistiqueFiliereDto stat : stats) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(stat.getFiliere());
                row.createCell(1).setCellValue(stat.getPeriode());
                row.createCell(2).setCellValue(stat.getNombreEtudiants());
                row.createCell(3).setCellValue(stat.getNombreStages());
                row.createCell(4).setCellValue(stat.getTauxPlacement());
            }

            // Ajuster la largeur des colonnes automatiquement
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}