package com.mogou.client;

import com.mogou.dto.CandidatureValidationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "applications-service")
public interface ApplicationClient {

    /**
     * Appelle le service des candidatures pour récupérer les détails de validation.
     * Cet endpoint doit exister dans le 'applications-service'.
     * @param candidatureId L'ID de la candidature.
     * @return Les détails de validation.
     */
    @GetMapping("/api/candidatures/{id}/validation-details")
    CandidatureValidationDto getCandidatureForValidation(@PathVariable("id") Long candidatureId);
}