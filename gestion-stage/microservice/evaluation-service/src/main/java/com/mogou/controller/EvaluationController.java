package com.mogou.controller;

import com.mogou.dto.CreateEvaluationRequest;
import com.mogou.dto.EvaluationDto;
import com.mogou.dto.SubmitEvaluationRequest;
import com.mogou.services.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/create")
    public ResponseEntity<EvaluationDto> createEvaluation(@Valid @RequestBody CreateEvaluationRequest request) {
        EvaluationDto createdEvaluation = evaluationService.createEvaluation(request);
        return new ResponseEntity<>(createdEvaluation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<EvaluationDto> submitEvaluation(@PathVariable Long id, @RequestBody SubmitEvaluationRequest request) {
        EvaluationDto submittedEvaluation = evaluationService.submitEvaluation(id, request);
        return ResponseEntity.ok(submittedEvaluation);
    }

    @GetMapping("/stage/{stageId}")
    public ResponseEntity<List<EvaluationDto>> getEvaluationsForStage(@PathVariable Long stageId) {
        List<EvaluationDto> evaluations = evaluationService.getEvaluationsByStageId(stageId);
        return ResponseEntity.ok(evaluations);
    }

    // Endpoints for statistics and template configuration would go here in their own controllers
}
