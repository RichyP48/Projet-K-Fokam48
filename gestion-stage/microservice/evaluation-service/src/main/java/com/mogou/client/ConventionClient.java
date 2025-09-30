package com.mogou.client;

import com.mogou.dto.ConventionDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "conventions-service")
public interface ConventionClient {

    @GetMapping("/api/conventions/{id}/details") // Assurez-vous que cet endpoint existe dans conventions-service
    ConventionDetailsDto getConventionById(@PathVariable("id") Long id);
}
