package com.mogou.services;

import com.mogou.dto.CreateEvaluationRequest;
import com.mogou.dto.EvaluationDto;
import com.mogou.dto.EvaluationMapper;
import com.mogou.dto.SubmitEvaluationRequest;
import com.mogou.model.CritereEvaluation;
import com.mogou.model.Evaluation;
import com.mogou.model.EvaluationTemplate;
import com.mogou.model.TypeCritere;
import com.mogou.repository.EvaluationRepository;
import com.mogou.repository.EvaluationTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationTemplateRepository templateRepository;
    private final EvaluationMapper evaluationMapper;
    // private final ConventionClient conventionClient; // To be used to fetch stage details

    @Override
    public EvaluationDto createEvaluation(CreateEvaluationRequest request) {
        // TODO: Use ConventionClient to get stage details like 'filiere'
        String filiere = "Informatique"; // Hardcoded for now

        EvaluationTemplate template = templateRepository.findByFiliereAndTypeEvaluation(filiere, request.getType())
                .orElseThrow(() -> new EntityNotFoundException("Evaluation template not found for filiere: " + filiere));

        Evaluation evaluation = new Evaluation();
        evaluation.setStageId(request.getStageId());
        evaluation.setEvaluateurId(request.getEvaluateurId());
        evaluation.setEvalueId(request.getEvalueId());
        evaluation.setType(request.getType());
        evaluation.setDateEvaluation(LocalDateTime.now());

        // Initialize criteria with null values
        Map<String, Object> initialCriteria = new HashMap<>();
        template.getCriteres().forEach(critere -> initialCriteria.put(critere.getNom(), null));
        evaluation.setCriteresReponses(initialCriteria);

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return evaluationMapper.toDto(savedEvaluation, template.getCriteres());
    }

    @Override
    public EvaluationDto submitEvaluation(Long evaluationId, SubmitEvaluationRequest request) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with id: " + evaluationId));

        // TODO: Use ConventionClient to get stage details like 'filiere'
        String filiere = "Informatique"; // Hardcoded for now
        EvaluationTemplate template = templateRepository.findByFiliereAndTypeEvaluation(filiere, evaluation.getType())
                .orElseThrow(() -> new EntityNotFoundException("Evaluation template not found for filiere: " + filiere));

        // TODO: Add validation for required fields

        evaluation.setCriteresReponses(request.getCriteresReponses());
        evaluation.setCommentaires(request.getCommentaires());
        evaluation.setNoteGlobale(calculateWeightedAverage(request.getCriteresReponses(), template.getCriteres()));
        evaluation.setFinalisee(true);
        evaluation.setDateEvaluation(LocalDateTime.now());

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        // TODO: Call NotificationService and ReportingService via Feign clients

        return evaluationMapper.toDto(savedEvaluation, template.getCriteres());
    }

    @Override
    public List<EvaluationDto> getEvaluationsByStageId(Long stageId) {
        List<Evaluation> evaluations = evaluationRepository.findByStageId(stageId);
        // This is a simplification. A real implementation would need to fetch the correct template for each evaluation.
        return evaluations.stream()
                .map(e -> evaluationMapper.toDto(e, List.of()))
                .toList();
    }

    private Integer calculateWeightedAverage(Map<String, Object> responses, List<CritereEvaluation> criteres) {
        double totalPoints = 0;
        int totalWeight = 0;

        for (CritereEvaluation critere : criteres) {
            if (critere.getType() == TypeCritere.NOTE_SUR_5 || critere.getType() == TypeCritere.NOTE_SUR_10) {
                Object responseObj = responses.get(critere.getNom());
                if (responseObj instanceof Number) {
                    int note = ((Number) responseObj).intValue();
                    int poids = critere.getPoids() != null ? critere.getPoids() : 1;
                    totalPoints += note * poids;
                    totalWeight += poids;
                }
            }
        }
        return (totalWeight == 0) ? 0 : (int) Math.round(totalPoints / totalWeight);
    }
}
