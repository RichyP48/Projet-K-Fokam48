package com.mogou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistiques_filieres")
@Data
public class StatistiqueFiliere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String filiere;
    
    @Column(nullable = false)
    private String periode;
    
    @Column(name = "nombre_etudiants")
    private Integer nombreEtudiants;
    
    @Column(name = "nombre_stages")
    private Integer nombreStages;
    
    @Column(name = "nombre_candidatures")
    private Integer nombreCandidatures;
    
    @Column(name = "taux_placement")
    private Double tauxPlacement;
    
    @Column(name = "nombre_conventions")
    private Integer nombreConventions;
    
    @Column(name = "nombre_conventions_signees")
    private Integer nombreConventionsSignees;
    
    @Column(name = "taux_signature")
    private Double tauxSignature;
    
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.dateMaj = LocalDateTime.now();
    }
}