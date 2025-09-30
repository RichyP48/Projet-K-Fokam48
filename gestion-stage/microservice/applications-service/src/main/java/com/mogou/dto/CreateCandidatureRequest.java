package com.mogou.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCandidatureRequest {
    @NotNull(message = "L'ID de l'étudiant est requis.")
    private Long etudiantId;

    @NotNull(message = "L'ID de l'offre est requis.")
    private Long offreId;

    private String commentaires;
}
