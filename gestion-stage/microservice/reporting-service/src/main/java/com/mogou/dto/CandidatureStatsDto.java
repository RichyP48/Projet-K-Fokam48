package com.mogou.dto;

import lombok.Data;

@Data
public class CandidatureStatsDto {
    private String filiere;
    private Integer nombreCandidatures;
    private Integer nombreAcceptees;
    private Double tauxAcceptation;
}
