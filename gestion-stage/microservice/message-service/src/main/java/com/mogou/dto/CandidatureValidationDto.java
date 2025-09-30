package com.mogou.dto;

import lombok.Data;
/**
 * DTO transportant les informations de validation d'une candidature
 * depuis le 'applications-service'.
 */
@Data
public class CandidatureValidationDto {
    private Long candidatureId;
    private Long etudiantId;
    private Long entrepriseId;
    private boolean estAcceptee;
}
