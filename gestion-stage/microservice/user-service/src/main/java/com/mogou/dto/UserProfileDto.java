package com.mogou.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserProfileDto {
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    
    // Informations académiques pour étudiants
    private Long schoolId;
    private Long facultyId;
    private Long departmentId;
    private String studyLevel; // L1, L2, L3, M1, M2, etc.
    
    // Champ flexible pour les données spécifiques au rôle
    private Map<String, Object> specificAttributes;
}
