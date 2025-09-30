package com.mogou.controller;

import com.mogou.dto.CandidatureDto;
import com.mogou.dto.CandidatureMapper;
import com.mogou.model.Candidature;
import com.mogou.service.CandidatureService;
import com.mogou.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidatures/{id}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final CandidatureService candidatureService;
    private final FileStorageService fileStorageService;

    /**
     * Uploade un document (CV ou lettre de motivation) et l'associe à une candidature.
     * @param id L'ID de la candidature.
     * @param file Le fichier à uploader.
     * @param type Le type de document, doit être "cv" ou "lettreMotivation".
     * @return Un DTO de la candidature mise à jour.
     */
    @PostMapping
    public ResponseEntity<CandidatureDto> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        Candidature candidatureMiseAJour = candidatureService.attachDocument(id, file, type);
        // On convertit l'entité en DTO avant de retourner la réponse.
        return ResponseEntity.ok(CandidatureMapper.toDto(candidatureMiseAJour));
    }

    /**
     * Télécharge un document associé à une candidature.
     * @param id L'ID de la candidature.
     * @param type Le type de document à télécharger ("cv" ou "lettreMotivation").
     * @return Le fichier en tant que flux d'octets.
     */
    @GetMapping("/{type}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id, @PathVariable String type) {
        Candidature candidature = candidatureService.findById(id);
        String filePath;

        if ("cv".equalsIgnoreCase(type)) {
            filePath = candidature.getCvPath();
        } else if ("lettreMotivation".equalsIgnoreCase(type)) {
            filePath = candidature.getLettreMotivationPath();
        } else {
            // Un message d'erreur plus clair pourrait être renvoyé ici
            // via le GlobalExceptionHandler.
            return ResponseEntity.badRequest().build();
        }

        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = fileStorageService.downloadFile(filePath);
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
