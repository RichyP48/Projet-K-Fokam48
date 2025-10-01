package com.mogou.client;

import lombok.Data;

@Data
public class OfferDto {
    private Long id;
    private String titre;
    private String description;
    private String localisation;
    private Integer duree;
    private String domaine;
    private String companyName;
    private Long entrepriseId;
}