package com.mogou.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateConventionRequest {
    @NotNull
    private Long candidatureId;

    @NotNull
    private ConventionDataDto conventionData;

    @NotNull
    private Long enseignantId;
}