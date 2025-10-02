package com.mogou.dto;

import com.mogou.model.DomaineStage;
import com.mogou.model.StatutOffre;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OffreStageDto {
    private Long id;
    private String titre;
    private String description;
    private Long entrepriseId;
    private String companyName; // Nom de l'entreprise
    private DomaineStage domaine;
    private Integer duree;
    private Double salaire;
    private String localisation;
    private String competencesRequises;
    private StatutOffre statut;
    private LocalDate datePublication;
    private LocalDate dateExpiration;
    private Integer applicationCount; // Nombre de candidatures
}