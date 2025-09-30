package com.mogou.dto;

import lombok.Data;

@Data
public class ConventionDataDto {
    // Infos étudiant
    private Long etudiantId;
    private String nomEtudiant;
    private String filiereEtudiant;
    private String niveauEtudiant;

    // Infos entreprise
    private Long entrepriseId;
    private String nomEntreprise;
    private String secteurEntreprise;
    private String adresseEntreprise;

    // Infos stage
    private String dureeStage;
    private String missionsStage;
    private String competences;

    // Infos tuteur
    private Long enseignantId;
    private String nomEnseignant;
}
