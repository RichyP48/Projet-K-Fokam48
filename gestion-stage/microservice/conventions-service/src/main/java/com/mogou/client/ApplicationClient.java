package com.mogou.client;

import com.mogou.dto.CandidatureDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "applications-service")
public interface ApplicationClient {
    @GetMapping("/api/candidatures/{id}")
    CandidatureDetailsDto getCandidatureById(@PathVariable("id") Long id);
    
    @GetMapping("/api/candidatures/entreprise/{entrepriseId}")
    List<CandidatureDetailsDto> getCandidaturesByEntreprise(@PathVariable("entrepriseId") Long entrepriseId);
}
