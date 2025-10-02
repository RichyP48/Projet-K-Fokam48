package com.mogou.service;

import com.mogou.dto.CandidatureDto;
import com.mogou.dto.CandidatureMapper;
import com.mogou.dto.CreateCandidatureRequest;
import com.mogou.enums.StatutCandidature;
import com.mogou.exceptions.CandidatureNotFoundException;
import com.mogou.model.Candidature;
import com.mogou.repository.CandidatureRepository;
import com.mogou.statemachine.CandidatureEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidatureServiceImpl implements CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final StateMachineService stateMachineService;
    private final FileStorageService fileStorageService;
    private final com.mogou.client.OffersClient offersClient;

    @Override
    @Transactional
    public Candidature create(CreateCandidatureRequest request) {
        if (candidatureRepository.existsByEtudiantIdAndOffreId(request.getEtudiantId(), request.getOffreId())) {
            throw new IllegalStateException("Une candidature pour cette offre existe déjà pour cet étudiant.");
        }

        Candidature candidature = Candidature.builder()
                .etudiantId(request.getEtudiantId())
                .offreId(request.getOffreId())
                .statut(StatutCandidature.POSTULE)
                .datePostulation(LocalDateTime.now())
                .commentaires(request.getCommentaires())
                .build();

        Candidature savedCandidature = candidatureRepository.save(candidature);
        stateMachineService.createInitialHistory(savedCandidature);

        return savedCandidature;
    }

    @Override
    @Transactional
    public Candidature changeStatus(Long candidatureId, CandidatureEvent event, String commentaire, Long userId) {
        Candidature candidature = findById(candidatureId);
        Candidature updatedCandidature = stateMachineService.sendEvent(candidature, event, commentaire, userId);
        return candidatureRepository.save(updatedCandidature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidature> findByEtudiantId(Long etudiantId) {
        return candidatureRepository.findByEtudiantId(etudiantId);
    }
    
    public List<CandidatureDto> findEnrichedByEtudiantId(Long etudiantId) {
        List<Candidature> candidatures = candidatureRepository.findByEtudiantId(etudiantId);
        return candidatures.stream()
                .map(this::enrichCandidatureWithOfferInfo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private CandidatureDto enrichCandidatureWithOfferInfo(Candidature candidature) {
        CandidatureDto dto = CandidatureMapper.toDto(candidature);
        try {
            com.mogou.client.OfferDto offer = offersClient.getOfferById(candidature.getOffreId());
            dto.setOfferTitle(offer.getTitre());
            dto.setCompanyName(offer.getCompanyName());
            dto.setLocation(offer.getLocalisation());
            dto.setDuration(offer.getDuree() != null ? String.valueOf(offer.getDuree()) : null);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'offre " + candidature.getOffreId() + ": " + e.getMessage());
            // Valeurs par défaut si l'offre n'existe plus
            dto.setOfferTitle("Offre supprimée (ID: " + candidature.getOffreId() + ")");
            dto.setCompanyName("Entreprise inconnue");
            dto.setLocation("Non spécifié");
            dto.setDuration("Non spécifié");
        }
        
        // Ajouter le nom de l'étudiant
        dto.setStudentName("Étudiant " + candidature.getEtudiantId());
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidature> findByOffreId(Long offreId) {
        return candidatureRepository.findByOffreId(offreId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidature> findByEntrepriseId(Long entrepriseId) {
        try {
            System.out.println("🔍 Looking for applications for company: " + entrepriseId);
            
            // Récupérer les offres de l'entreprise
            List<com.mogou.client.OfferDto> offers;
            try {
                offers = offersClient.getOffersByCompanyId(entrepriseId);
                System.out.println("📋 Found " + offers.size() + " offers for company " + entrepriseId);
            } catch (feign.FeignException.NotFound e) {
                System.out.println("❌ Company " + entrepriseId + " not found or has no offers");
                return List.of();
            } catch (Exception e) {
                System.err.println("❌ Error calling offers-service for company " + entrepriseId + ": " + e.getMessage());
                return List.of();
            }
            
            if (offers == null || offers.isEmpty()) {
                System.out.println("❌ No offers found for company " + entrepriseId);
                return List.of();
            }
            
            List<Long> offerIds = offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList());
            System.out.println("🎯 Offer IDs: " + offerIds);
            
            // Récupérer toutes les candidatures pour ces offres
            List<Candidature> candidatures = candidatureRepository.findByOffreIdIn(offerIds);
            System.out.println("📝 Found " + candidatures.size() + " applications for company " + entrepriseId);
            
            return candidatures;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error getting applications for company " + entrepriseId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<CandidatureDto> findEnrichedByEntrepriseId(Long entrepriseId) {
        List<Candidature> candidatures = findByEntrepriseId(entrepriseId);
        return candidatures.stream()
                .map(this::enrichCandidatureWithOfferInfo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional
    public Candidature acceptApplication(Long candidatureId, Long entrepriseId) {
        Candidature candidature = findById(candidatureId);
        
        // Update status to ACCEPTED
        candidature.setStatut(com.mogou.enums.StatutCandidature.ACCEPTE);
        Candidature savedCandidature = candidatureRepository.save(candidature);
        
        // Trigger convention generation
        try {
            generateConventionForApplication(savedCandidature);
        } catch (Exception e) {
            System.err.println("Error generating convention: " + e.getMessage());
        }
        
        return savedCandidature;
    }
    
    @Transactional
    public Candidature rejectApplication(Long candidatureId, Long entrepriseId, String reason) {
        Candidature candidature = findById(candidatureId);
        
        // Update status to REJECTED
        candidature.setStatut(com.mogou.enums.StatutCandidature.REFUSE);
        candidature.setCommentaires(candidature.getCommentaires() + "\n\nRejection reason: " + reason);
        
        return candidatureRepository.save(candidature);
    }
    
    private void generateConventionForApplication(Candidature candidature) {
        try {
            // Get offer details
            com.mogou.client.OfferDto offer = offersClient.getOfferById(candidature.getOffreId());
            
            // Create convention generation request
            Map<String, Object> conventionRequest = new java.util.HashMap<>();
            conventionRequest.put("candidatureId", candidature.getId());
            conventionRequest.put("etudiantId", candidature.getEtudiantId());
            conventionRequest.put("entrepriseId", offer.getEntrepriseId());
            conventionRequest.put("offreId", candidature.getOffreId());
            conventionRequest.put("titre", offer.getTitre());
            conventionRequest.put("duree", offer.getDuree());
            conventionRequest.put("dateDebut", java.time.LocalDate.now().plusDays(30)); // Start in 30 days
            
            // TODO: Call conventions service to generate convention
            System.out.println("Convention generation triggered for application: " + candidature.getId());
            
        } catch (Exception e) {
            System.err.println("Failed to generate convention: " + e.getMessage());
            throw new RuntimeException("Convention generation failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Candidature findById(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'id : " + id));
    }

    public com.mogou.client.OffersClient getOffersClient() {
        return offersClient;
    }
    
    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    @Override
    @Transactional
    public Candidature attachDocument(Long candidatureId, MultipartFile file, String documentType) {
        Candidature candidature = findById(candidatureId);
        String filePath = fileStorageService.uploadFile(file, candidature.getEtudiantId());

        if ("cv".equalsIgnoreCase(documentType)) {
            candidature.setCvPath(filePath);
        } else if ("lettreMotivation".equalsIgnoreCase(documentType)) {
            candidature.setLettreMotivationPath(filePath);
        } else {
            throw new IllegalArgumentException("Type de document non valide : " + documentType + ". Utilisez 'cv' ou 'lettreMotivation'.");
        }
        return candidatureRepository.save(candidature);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countByOffreId(Long offreId) {
        return candidatureRepository.countByOffreId(offreId);
    }
}