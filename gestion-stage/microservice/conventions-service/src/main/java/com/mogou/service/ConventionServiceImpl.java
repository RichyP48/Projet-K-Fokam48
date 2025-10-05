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
    private final com.mogou.jwt.JwtUtil jwtUtil;

    @Override
    @Transactional
    public Convention generate(GenerateConventionRequest request) {
        // 1. Récupérer les données de la candidature
        CandidatureDetailsDto candidature = applicationClient.getCandidatureById(request.getCandidatureId());
        
        // 2. SECURITY CHECK: Verify ownership
        Long currentEntrepriseId = getCurrentUserId();
        if (currentEntrepriseId == null) {
            throw new SecurityException("Unable to identify current user");
        }
        if (candidature.getEntrepriseId() != null && !candidature.getEntrepriseId().equals(currentEntrepriseId)) {
            throw new SecurityException("Action non autorisée: la candidature n'appartient pas à cette entreprise.");
        }
        
        // 3. Utiliser les données fournies dans le DTO (pas besoin d'appeler user-service)
        ConventionDataDto dataForPdf = request.getConventionData();

        try {
            // 4. Générer le PDF en mémoire
            byte[] pdfBytes = pdfGenerationService.generateConventionPdf(dataForPdf);
            String documentPath = "conventions/" + UUID.randomUUID() + ".pdf";

            // 5. Uploader le PDF vers MinIO
            fileStorageService.uploadFile(new ByteArrayInputStream(pdfBytes), documentPath, "application/pdf");

            // 6. Créer l'entité Convention
            System.out.println("🚨 EntrepriseId avant save = " + currentEntrepriseId);
            Convention convention = Convention.builder()
                    .candidatureId(request.getCandidatureId())
                    .etudiantId(dataForPdf.getEtudiantId())
                    .enseignantId(request.getEnseignantId())
                    .entrepriseId(currentEntrepriseId)
                    .statut(StatutConvention.EN_ATTENTE_VALIDATION)
                    .dateCreation(LocalDateTime.now())
                    .documentPath(documentPath)
                    .build();

            Convention saved = conventionRepository.save(convention);
            System.out.println("✅ Convention saved with entrepriseId = " + saved.getEntrepriseId());
            return saved;
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
        try {
            // Récupérer les candidatures de l'entreprise puis les conventions associées
            List<CandidatureDetailsDto> candidatures = applicationClient.getCandidaturesByEntreprise();
            List<Long> candidatureIds = candidatures.stream().map(CandidatureDetailsDto::getId).toList();
            System.out.println("[CONVENTIONS] Found " + candidatureIds.size() + " candidature IDs: " + candidatureIds);
            return conventionRepository.findByCandidatureIdIn(candidatureIds);
        } catch (Exception e) {
            System.out.println("[CONVENTIONS] Error calling applications service: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }



    @Override
    public byte[] getConventionPdf(Long id) {
        Convention convention = findById(id);
        try {
            return fileStorageService.downloadFile(convention.getDocumentPath());
        } catch (Exception e) {
            System.out.println("PDF not found: " + convention.getDocumentPath());
            return new byte[0]; // Return empty byte array instead of throwing exception
        }
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
    
    private Long getCurrentUserId() {
        try {
            // Utiliser le header X-User-Id propagé par l'API Gateway
            org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    Long userId = Long.valueOf(userIdHeader);
                    System.out.println("🚨 EntrepriseId extrait du header X-User-Id = " + userId);
                    return userId;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from request: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}