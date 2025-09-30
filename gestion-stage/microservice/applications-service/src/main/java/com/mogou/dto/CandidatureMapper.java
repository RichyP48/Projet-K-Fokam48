package com.mogou.dto;

import com.mogou.model.Candidature;

import java.util.List;
import java.util.stream.Collectors;

public class CandidatureMapper {

    public static CandidatureDto toDto(Candidature entity) {
        if (entity == null) {
            return null;
        }
        return CandidatureDto.builder()
                .id(entity.getId())
                .etudiantId(entity.getEtudiantId())
                .offreId(entity.getOffreId())
                .statut(entity.getStatut())
                .datePostulation(entity.getDatePostulation())
                .commentaires(entity.getCommentaires())
                .hasCv(entity.getCvPath() != null && !entity.getCvPath().isBlank())
                .hasLettreMotivation(entity.getLettreMotivationPath() != null && !entity.getLettreMotivationPath().isBlank())
                .build();
    }

    public static List<CandidatureDto> toDtoList(List<Candidature> entities) {
        return entities.stream()
                .map(CandidatureMapper::toDto)
                .collect(Collectors.toList());
    }
}