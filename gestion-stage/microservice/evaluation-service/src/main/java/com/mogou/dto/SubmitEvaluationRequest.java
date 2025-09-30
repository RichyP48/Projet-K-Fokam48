package com.mogou.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitEvaluationRequest {
    private Map<String, Object> criteresReponses;
    private String commentaires;
}