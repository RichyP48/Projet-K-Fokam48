package com.mogou.dto;

import com.mogou.model.Candidature;
import com.mogou.enums.StatutCandidature;

import java.util.List;
import java.util.stream.Collectors;

public class CandidatureMapper {

    public static CandidatureDto toDto(Candidature entity) {
        if (entity == null) {
            return null;
        }
        
        // Mapper le statut vers le format anglais attendu par le frontend
        String statusEnglish = mapStatusToEnglish(entity.getStatut());
        
        return CandidatureDto.builder()
                .id(entity.getId())
                .etudiantId(entity.getEtudiantId())
                .offreId(entity.getOffreId())
                .statut(entity.getStatut())
                .datePostulation(entity.getDatePostulation())
                .commentaires(entity.getCommentaires())
                .hasCv(entity.getCvPath() != null && !entity.getCvPath().isBlank())
                .hasLettreMotivation(entity.getLettreMotivationPath() != null && !entity.getLettreMotivationPath().isBlank())
                // Champs pour compatibilité frontend
                .studentId(entity.getEtudiantId())
                .offerId(entity.getOffreId())
                .status(statusEnglish)
                .applicationDate(entity.getDatePostulation())
                .coverLetter(entity.getCommentaires())
                .cvPath(entity.getCvPath())
                .build();
    }
    
    private static String mapStatusToEnglish(StatutCandidature statut) {
        switch (statut) {
            case POSTULE: return "PENDING";
            case EN_ATTENTE: return "PENDING";
            case ACCEPTE: return "ACCEPTED";
            case REFUSE: return "REJECTED";
            case RETIRE: return "WITHDRAWN";
            default: return "PENDING";
        }
    }

    public static List<CandidatureDto> toDtoList(List<Candidature> entities) {
        return entities.stream()
                .map(CandidatureMapper::toDto)
                .collect(Collectors.toList());
    }
}