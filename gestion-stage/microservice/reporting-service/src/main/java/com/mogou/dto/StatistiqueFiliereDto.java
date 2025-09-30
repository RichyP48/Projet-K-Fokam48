package com.mogou.dto;

import lombok.Data;

/**
 * DTO (Data Transfer Object) pour transporter les statistiques
 * agrégées par filière académique.
 */
@Data
public class StatistiqueFiliereDto {

    private String filiere;
    private String periode;
    private int nombreEtudiants;
    private int nombreStages;
    private double tauxPlacement; // Exprimé en pourcentage (ex: 85.5)
    private int nombreCandidatures; // Ajout d'une métrique supplémentaire
    private int nombreConventions;
    private int nombreConventionsSignees;
    private double tauxSignature; // Pourcentage de conventions signées
}
