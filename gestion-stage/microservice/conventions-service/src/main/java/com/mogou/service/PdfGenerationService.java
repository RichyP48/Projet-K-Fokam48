package com.mogou.service;

import com.mogou.dto.ConventionDataDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGenerationService {

    public byte[] generateConventionPdf(ConventionDataDto data) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // L'API de PDFBox 3.x est plus simple.
                // On se positionne et on écrit le texte.

                // Titre
                addText(contentStream, fontBold, 18, 150, 750, "CONVENTION DE STAGE");

                // Section Étudiant
                addText(contentStream, fontBold, 12, 50, 680, "Partie 1: L'Étudiant");
                addText(contentStream, font, 10, 60, 660, "Nom complet: " + data.getNomEtudiant());
                addText(contentStream, font, 10, 60, 645, "Filière: " + data.getFiliereEtudiant());

                // Section Entreprise (à compléter avec les données du DTO)
                addText(contentStream, fontBold, 12, 50, 600, "Partie 2: L'Entreprise d'Accueil");
                addText(contentStream, font, 10, 60, 580, "Raison sociale: ");
                addText(contentStream, font, 10, 60, 565, "Adresse: ");

                // ... Ajouter d'autres sections (missions, tuteur, clauses, etc.)

                addText(contentStream, font, 8, 50, 50, "Document généré le " + java.time.LocalDate.now());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Utilitaire pour ajouter du texte à une position donnée dans le PDF.
     */
    private void addText(PDPageContentStream contentStream, PDType1Font font, float fontSize, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}
