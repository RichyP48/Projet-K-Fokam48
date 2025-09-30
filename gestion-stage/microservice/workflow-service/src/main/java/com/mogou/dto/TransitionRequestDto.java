package com.mogou.dto;

import com.mogou.model.StageEvent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO (Data Transfer Object) pour transporter une demande de transition
 * d'état dans le corps d'une requête POST.
 */
@Data
public class TransitionRequestDto {

    @NotNull(message = "L'événement de transition ne peut pas être nul.")
    private StageEvent event;
}