package com.mogou.dto;

import lombok.Data;

@Data
public class CandidatureDetailsDto {
    private Long id;
    private Long etudiantId;
    private Long offreId;
    private String statut; // ex: "ACCEPTEE"
}
