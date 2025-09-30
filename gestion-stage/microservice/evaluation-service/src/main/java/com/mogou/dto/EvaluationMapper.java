package com.mogou.dto;

import com.mogou.model.CritereEvaluation;
import com.mogou.model.Evaluation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EvaluationMapper {

    public EvaluationDto toDto(Evaluation evaluation, List<CritereEvaluation> criteresDefinition) {
        EvaluationDto dto = new EvaluationDto();
        dto.setId(evaluation.getId());
        dto.setStageId(evaluation.getStageId());
        dto.setEvaluateurId(evaluation.getEvaluateurId());
        dto.setEvalueId(evaluation.getEvalueId());
        dto.setType(evaluation.getType());
        dto.setCriteresReponses(evaluation.getCriteresReponses());
        dto.setCriteresDefinition(criteresDefinition); // Add the questions schema
        dto.setNoteGlobale(evaluation.getNoteGlobale());
        dto.setCommentaires(evaluation.getCommentaires());
        dto.setDateEvaluation(evaluation.getDateEvaluation());
        dto.setFinalisee(evaluation.isFinalisee());
        return dto;
    }
}