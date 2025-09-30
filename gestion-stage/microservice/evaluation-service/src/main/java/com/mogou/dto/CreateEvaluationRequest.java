package com.mogou.dto;

import com.mogou.model.TypeEvaluation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEvaluationRequest {
    @NotNull
    private Long stageId;
    @NotNull
    private Long evaluateurId;
    @NotNull
    private Long evalueId;
    @NotNull
    private TypeEvaluation type;
}