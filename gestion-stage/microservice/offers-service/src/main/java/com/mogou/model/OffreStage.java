package com.mogou.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class OffreStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long entrepriseId;

    @Enumerated(EnumType.STRING)
    private DomaineStage domaine;

    private Integer duree; // en mois
    
    private Double salaire; // salaire mensuel

    private String localisation;

    private String competencesRequises;

    @Enumerated(EnumType.STRING)
    private StatutOffre statut;

    private LocalDate datePublication;
    private LocalDate dateExpiration;
}