package com.mogou.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CritereEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // Ex: "Communication", "Autonomie"
    private String description;

    @Enumerated(EnumType.STRING)
    private TypeCritere type; // NOTE, TEXTE, CHOIX_MULTIPLE

    private Integer poids; // Ex: 2 (compte double dans la moyenne)
    private boolean obligatoire;
}
