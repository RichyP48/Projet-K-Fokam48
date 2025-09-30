package com.mogou.dto;

import com.mogou.model.TypeSignataire;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignConventionRequest {
    private Long signataireId;
    private String typeSignataire; // CORRIGÉ : TypeSignataire -> String
    private String ipAddress;
}