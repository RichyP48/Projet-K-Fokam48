package com.mogou.dto;

import com.mogou.enums.StatutCandidature;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CandidatureDto {
    private Long id;
    private Long etudiantId;
    private Long offreId;
    private Long entrepriseId;
    private StatutCandidature statut;
    private LocalDateTime datePostulation;
    private boolean hasCv;
    private boolean hasLettreMotivation;
    private String commentaires;
    
    // Informations enrichies de l'offre
    private String offerTitle;
    private String companyName;
    private String location;
    private String duration;
    
    // Champs pour compatibilité frontend (mapping anglais)
    private Long studentId;
    private Long offerId;
    private Long companyId;
    private String status;
    private LocalDateTime applicationDate;
    private String coverLetter;
    private String studentName;
    private String cvPath;
}