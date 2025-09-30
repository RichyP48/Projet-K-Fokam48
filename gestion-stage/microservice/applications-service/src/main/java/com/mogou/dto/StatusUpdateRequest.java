package com.mogou.dto;

import com.mogou.statemachine.CandidatureEvent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull
    private CandidatureEvent event;
    private String commentaire;
    @NotNull
    private Long userId;
}