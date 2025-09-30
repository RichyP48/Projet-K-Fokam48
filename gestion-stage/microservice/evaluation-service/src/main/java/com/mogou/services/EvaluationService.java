package com.mogou.services;

import com.mogou.dto.CreateEvaluationRequest;
import com.mogou.dto.EvaluationDto;
import com.mogou.dto.SubmitEvaluationRequest;

import java.util.List;

public interface EvaluationService {
    EvaluationDto createEvaluation(CreateEvaluationRequest request);
    EvaluationDto submitEvaluation(Long evaluationId, SubmitEvaluationRequest request);
    List<EvaluationDto> getEvaluationsByStageId(Long stageId);
}
