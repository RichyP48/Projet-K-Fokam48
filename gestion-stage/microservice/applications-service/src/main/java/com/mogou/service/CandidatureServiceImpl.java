package com.mogou.service;

import com.mogou.dto.CandidatureDto;
import com.mogou.dto.CandidatureMapper;
import com.mogou.dto.CreateCandidatureRequest;
import com.mogou.enums.StatutCandidature;
import com.mogou.exceptions.CandidatureNotFoundException;
import com.mogou.model.Candidature;
import com.mogou.repository.CandidatureRepository;
import com.mogou.statemachine.CandidatureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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
            dto.setDuration(offer.getDuree());
        } catch (Exception e) {
            // En cas d'erreur, on garde les valeurs par défaut (null)
            System.err.println("Erreur lors de la récupération de l'offre " + candidature.getOffreId() + ": " + e.getMessage());
        }
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
        // TODO: Implémenter la logique pour récupérer les candidatures par entreprise
        // Il faut passer par les offres pour trouver les candidatures liées à une entreprise
        return List.of(); // Temporaire
    }

    @Override
    @Transactional(readOnly = true)
    public Candidature findById(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'id : " + id));
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
}
