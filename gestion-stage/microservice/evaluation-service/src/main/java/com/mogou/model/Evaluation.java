package com.mogou.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long stageId;
    private Long evaluateurId; // ID de l'étudiant ou du tuteur entreprise
    private Long evalueId; // ID de l'étudiant (si évalué) ou du stage (si auto-évalué)

    @Enumerated(EnumType.STRING)
    private TypeEvaluation type;

    // Stocke les réponses (critereId -> reponse) dans une colonne JSONB
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> criteresReponses;

    private Integer noteGlobale;
    private String commentaires;
    private LocalDateTime dateEvaluation;
    private boolean isFinalisee = false;
}

