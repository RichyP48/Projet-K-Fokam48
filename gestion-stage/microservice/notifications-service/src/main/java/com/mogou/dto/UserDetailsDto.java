package com.mogou.dto;

import lombok.Data;

/**
 * DTO représentant les informations d'un utilisateur
 * récupérées depuis le user-service.
 */
@Data
public class UserDetailsDto {
    private Long id;
    private String nomComplet;
    private String email;
}
