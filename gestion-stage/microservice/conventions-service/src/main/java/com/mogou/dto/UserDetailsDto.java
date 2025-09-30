package com.mogou.dto;

import lombok.Data;

@Data
public class UserDetailsDto {
    private Long id;
    private String nomComplet;
    private String email;
    private String filiere; // Pour un étudiant
    private String niveauEtudes; // Pour un étudiant
}