package com.mogou.controller;


import com.mogou.dto.TransitionRequestDto;
import com.mogou.model.StageState;
import com.mogou.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/stage")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/{candidatureId}/status")
    public ResponseEntity<Map<String, StageState>> getStatus(@PathVariable Long candidatureId) {
        return ResponseEntity.ok(Map.of("status", workflowService.getCurrentState(candidatureId)));
    }

    @PostMapping("/{candidatureId}/transition")
    public ResponseEntity<Map<String, String>> transition(
            @PathVariable Long candidatureId,
            @Valid @RequestBody TransitionRequestDto request) { // <-- @Valid ajouté pour la validation

        // CET APPEL EST MAINTENANT VALIDE
        boolean success = workflowService.sendEvent(candidatureId, request.getEvent());

        if (success) {
            String newState = workflowService.getCurrentState(candidatureId).name();
            return ResponseEntity.ok(Map.of("message", "Transition réussie", "newState", newState));
        } else {
            String currentState = workflowService.getCurrentState(candidatureId).name();
            return ResponseEntity.badRequest().body(Map.of("message", "Transition invalide depuis l'état " + currentState));
        }
    }
}
