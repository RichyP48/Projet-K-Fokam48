package com.mogou.service;

import com.mogou.client.ApplicationClient;
import com.mogou.client.UserClient;
import com.mogou.dto.*;
import com.mogou.model.Convention;
import com.mogou.model.SignatureConvention;
import com.mogou.model.StatutConvention;
import com.mogou.model.TypeSignataire;
import com.mogou.repository.ConventionRepository;
import com.mogou.repository.SignatureRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConventionServiceImpl implements ConventionService {

    private final ConventionRepository conventionRepository;
    private final SignatureRepository signatureRepository;
    private final PdfGenerationService pdfGenerationService;
    private final FileStorageService fileStorageService;
    private final ApplicationClient applicationClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public Convention generate(GenerateConventionRequest request) {
        // 1. Récupérer les données critiques via OpenFeign
        CandidatureDetailsDto candidature = applicationClient.getCandidatureById(request.getCandidatureId());
        UserDetailsDto etudiant = userClient.getUserById(candidature.getEtudiantId());


        // 2. Préparer les données pour le template PDF
        ConventionDataDto dataForPdf = new ConventionDataDto();
        dataForPdf.setNomEtudiant(etudiant.getNomComplet());
        dataForPdf.setFiliereEtudiant(etudiant.getFiliere());
        // ... Remplir ici toutes les autres données (entreprise, tuteur, missions, etc.)

        try {
            // 3. Générer le PDF en mémoire
            byte[] pdfBytes = pdfGenerationService.generateConventionPdf(dataForPdf);
            String documentPath = "conventions/" + UUID.randomUUID() + ".pdf";

            // 4. Uploader le PDF vers MinIO
            fileStorageService.uploadFile(new ByteArrayInputStream(pdfBytes), documentPath, "application/pdf");

            // 5. Créer l'entité Convention
            Convention convention = Convention.builder()
                    .candidatureId(request.getCandidatureId())
                    .etudiantId(etudiant.getId())
                    .enseignantId(request.getEnseignantId())
                    .statut(StatutConvention.EN_ATTENTE_VALIDATION)
                    .dateCreation(LocalDateTime.now())
                    .documentPath(documentPath)
                    .build();

            return conventionRepository.save(convention);
        } catch (IOException e) {
            throw new RuntimeException("Échec de la génération ou de l'upload du PDF", e);
        }
    }

    @Override
    @Transactional
    public Convention validate(Long conventionId, Long enseignantId) {
        Convention convention = findById(conventionId);
        if (!convention.getEnseignantId().equals(enseignantId)) {
            throw new SecurityException("Action non autorisée : cet enseignant ne peut pas valider cette convention.");
        }
        convention.setStatut(StatutConvention.VALIDEE);
        convention.setDateValidation(LocalDateTime.now());
        return conventionRepository.save(convention);
    }

    @Override
    @Transactional
    public Convention sign(Long conventionId, SignConventionRequest request, HttpServletRequest httpServletRequest) {
        Convention convention = findById(conventionId);
        byte[] pdfBytes = getConventionPdf(conventionId);
        String documentHash = calculateSHA256(pdfBytes);

        SignatureConvention signature = SignatureConvention.builder()
                .convention(convention)
                .signataireId(request.getSignataireId())
                .typeSignataire(TypeSignataire.valueOf(request.getTypeSignataire().toUpperCase()))
                .dateSignature(LocalDateTime.now())
                .ipAddress(httpServletRequest.getRemoteAddr())
                .documentHash(documentHash)
                .build();
        signatureRepository.save(signature);

        // Mettre à jour le statut après signature
        long signCount = signatureRepository.countByConvention_Id(conventionId); // CORRIGÉ
        if (signCount >= 2) { // Supposons 2 signatures requises (étudiant, entreprise)
            convention.setStatut(StatutConvention.FINALISEE);
        } else {
            convention.setStatut(StatutConvention.SIGNEE);
        }

        return conventionRepository.save(convention);
    }

    @Override
    public Convention findById(Long id) {
        return conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID : " + id));
    }

    @Override
    public List<Convention> findByEtudiantId(Long etudiantId) {
        return conventionRepository.findByEtudiantId(etudiantId);
    }

    @Override
    public List<Convention> findByEnseignantId(Long enseignantId) {
        return conventionRepository.findByEnseignantId(enseignantId);
    }
    
    @Override
    public List<Convention> findByEntrepriseId(Long entrepriseId) {
        // Récupérer les candidatures de l'entreprise puis les conventions associées
        List<CandidatureDetailsDto> candidatures = applicationClient.getCandidaturesByEntreprise(entrepriseId);
        List<Long> candidatureIds = candidatures.stream().map(CandidatureDetailsDto::getId).toList();
        return conventionRepository.findByCandidatureIdIn(candidatureIds);
    }



    @Override
    public byte[] getConventionPdf(Long id) {
        Convention convention = findById(id);
        return fileStorageService.downloadFile(convention.getDocumentPath());
    }

    @Override
    public List<Convention> findAll() {
        return conventionRepository.findAll();
    }

    @Override
    public List<Convention> findPending() {
        return conventionRepository.findByStatut(StatutConvention.EN_ATTENTE_VALIDATION);
    }

    @Override
    public void testMinioConnection() {
        // Test simple de connexion MinIO
        fileStorageService.uploadFile(new ByteArrayInputStream("test".getBytes()), "test/connection-test.txt", "text/plain");
    }

    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme SHA-256 non trouvé", e);
        }
    }
}