package com.mogou.dto;

import com.mogou.model.DomaineStage;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOffreStageRequest {
    @NotBlank(message = "Le titre est obligatoire.")
    private String titre;

    @NotBlank(message = "La description est obligatoire.")
    private String description;

    @NotNull(message = "L'ID de l'entreprise est obligatoire.")
    private Long entrepriseId;

    @NotNull(message = "Le domaine est obligatoire.")
    private DomaineStage domaine;

    @NotNull(message = "La durée est obligatoire.")
    @Min(value = 1, message = "La durée doit être d'au moins 1 mois.")
    private Integer duree;

    @NotBlank(message = "La localisation est obligatoire.")
    private String localisation;

    private String competencesRequises;

    @NotNull(message = "La date d'expiration est obligatoire.")
    private LocalDate dateExpiration;
}