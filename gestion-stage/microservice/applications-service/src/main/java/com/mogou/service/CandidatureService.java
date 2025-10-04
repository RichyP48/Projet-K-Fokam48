package com.mogou.service;

import com.mogou.dto.CreateCandidatureRequest;
import com.mogou.model.Candidature;
import com.mogou.statemachine.CandidatureEvent;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidatureService {
    Candidature create(CreateCandidatureRequest request);
    Candidature changeStatus(Long candidatureId, CandidatureEvent event, String commentaire, Long userId);
    List<Candidature> findByEtudiantId(Long etudiantId);
    List<Candidature> findByOffreId(Long offreId);
    List<Candidature> findByEntrepriseId(Long entrepriseId);
    Candidature findById(Long id);
    Candidature attachDocument(Long candidatureId, MultipartFile file, String documentType);
    Candidature acceptApplication(Long candidatureId, Long entrepriseId);
    Candidature rejectApplication(Long candidatureId, Long entrepriseId, String reason);
    Long countByOffreId(Long offreId);
}