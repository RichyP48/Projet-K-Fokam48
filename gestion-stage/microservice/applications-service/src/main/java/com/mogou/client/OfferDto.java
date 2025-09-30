package com.mogou.client;

import lombok.Data;

@Data
public class OfferDto {
    private Long id;
    private String titre;
    private String localisation;
    private String duree;
    private String domaine;
    private String companyName;
    private String description;
}