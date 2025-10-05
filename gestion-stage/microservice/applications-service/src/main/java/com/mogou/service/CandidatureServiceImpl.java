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
    private final com.mogou.client.ConventionsClient conventionsClient;

    @Override
    @Transactional
    public Candidature create(CreateCandidatureRequest request) {
        System.out.println("🚀 Creating candidature for student " + request.getEtudiantId() + " and offer " + request.getOffreId());
        
        // Validate request
        if (request.getEtudiantId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (request.getOffreId() == null) {
            throw new IllegalArgumentException("Offer ID cannot be null");
        }
        
        // Check for existing candidature
        try {
            if (candidatureRepository.existsByEtudiantIdAndOffreId(request.getEtudiantId(), request.getOffreId())) {
                throw new IllegalStateException("Une candidature pour cette offre existe déjà pour cet étudiant.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking existing candidature: " + e.getMessage());
            throw new RuntimeException("Database error while checking existing candidature: " + e.getMessage());
        }

        // Récupérer l'entrepriseId depuis l'offre
        Long entrepriseId;
        try {
            System.out.println("📞 Calling offers-service for offer ID: " + request.getOffreId());
            com.mogou.client.OfferDto offer = offersClient.getOfferById(request.getOffreId());
            if (offer == null) {
                throw new IllegalStateException("Offer not found with ID: " + request.getOffreId());
            }
            entrepriseId = offer.getEntrepriseId();
            if (entrepriseId == null) {
                throw new IllegalStateException("Offer has no company ID: " + request.getOffreId());
            }
            System.out.println("✅ Retrieved company ID: " + entrepriseId + " for offer: " + request.getOffreId());
        } catch (FeignException e) {
            System.err.println("❌ Feign error calling offers-service: " + e.getMessage());
            throw new IllegalStateException("Service communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error retrieving offer: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de récupérer les informations de l'offre: " + e.getMessage());
        }

        try {
            System.out.println("💾 Building candidature object...");
            Candidature candidature = Candidature.builder()
                    .etudiantId(request.getEtudiantId())
                    .offreId(request.getOffreId())
                    .entrepriseId(entrepriseId)
                    .statut(StatutCandidature.POSTULE)
                    .datePostulation(LocalDateTime.now())
                    .commentaires(request.getCommentaires())
                    .build();

            System.out.println("💾 Saving candidature to database...");
            Candidature savedCandidature = candidatureRepository.save(candidature);
            System.out.println("✅ Candidature saved with ID: " + savedCandidature.getId());
            
            System.out.println("📝 Creating initial history...");
            stateMachineService.createInitialHistory(savedCandidature);
            System.out.println("✅ Initial history created");

            return savedCandidature;
        } catch (Exception e) {
            System.err.println("❌ Error saving candidature: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while saving candidature: " + e.getMessage());
        }
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
    
    public CandidatureDto enrichCandidatureWithOfferInfo(Candidature candidature) {
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
        
        // S'assurer que les champs de compatibilité sont bien remplis
        dto.setStudentId(candidature.getEtudiantId());
        dto.setOfferId(candidature.getOffreId());
        dto.setApplicationDate(candidature.getDatePostulation());
        dto.setCoverLetter(candidature.getCommentaires());
        dto.setCvPath(candidature.getCvPath());
        
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
            
            // Utiliser directement le champ entrepriseId pour une requête plus efficace
            List<Candidature> candidatures = candidatureRepository.findByEntrepriseId(entrepriseId);
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
            com.mogou.client.ConventionsClient.GenerateConventionRequest request = 
                new com.mogou.client.ConventionsClient.GenerateConventionRequest();
            request.setCandidatureId(candidature.getId());
            request.setEtudiantId(candidature.getEtudiantId());
            request.setEntrepriseId(offer.getEntrepriseId());
            request.setOffreId(candidature.getOffreId());
            request.setTitre(offer.getTitre());
            request.setDuree(offer.getDuree());
            request.setDateDebut(java.time.LocalDate.now().plusDays(30).toString());
            
            // Call conventions service to generate convention
            com.mogou.client.ConventionsClient.ConventionDto convention = conventionsClient.generateConvention(request);
            System.out.println("✅ Convention generated successfully: ID=" + convention.getId() + ", Status=" + convention.getStatut());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to generate convention: " + e.getMessage());
            // Don't throw exception to avoid breaking the application acceptance flow
            System.err.println("⚠️ Application accepted but convention generation failed");
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
    
    /**
     * Validates if a user is authorized to access applications for a specific company
     */
    public boolean validateCompanyAccess(Long companyId, Long userId) {
        try {
            // Only the company itself can access its applications
            boolean authorized = companyId.equals(userId);
            
            System.out.println("🔒 Company access validation: Company " + companyId + ", User " + userId + ", Authorized: " + authorized);
            
            return authorized;
        } catch (Exception e) {
            System.err.println("❌ Company access validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates if a user is authorized to access a specific offer's applications
     */
    public boolean validateOfferAccess(Long offerId, Long userId) {
        try {
            // Get the offer details
            com.mogou.client.OfferDto offer = offersClient.getOfferById(offerId);
            
            // Only the company that owns the offer can access its applications
            boolean authorized = offer.getEntrepriseId().equals(userId);
            
            System.out.println("🔒 Offer access validation: Offer " + offerId + " owned by company " + offer.getEntrepriseId() + ", User " + userId + ", Authorized: " + authorized);
            
            return authorized;
        } catch (Exception e) {
            System.err.println("❌ Offer access validation failed: " + e.getMessage());
            return false;
        }
    }
}