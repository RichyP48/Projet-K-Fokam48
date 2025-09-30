package com.mogou.dto;

import com.mogou.model.CritereEvaluation;
import com.mogou.model.TypeEvaluation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EvaluationDto {
    private Long id;
    private Long stageId;
    private Long evaluateurId;
    private Long evalueId;
    private TypeEvaluation type;
    private Map<String, Object> criteresReponses;
    private List<CritereEvaluation> criteresDefinition; // To provide context to the frontend
    private Integer noteGlobale;
    private String commentaires;
    private LocalDateTime dateEvaluation;
    private boolean isFinalisee;
}

