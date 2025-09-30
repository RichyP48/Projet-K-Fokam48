package com.mogou.dto;

import lombok.Data;

@Data
public class ConventionStatsDto {
    private String filiere;
    private Integer nombreConventions;
    private Integer nombreSignees;
    private Integer nombreEnAttente;
    private Double tauxSignature;
}