package com.mogou.client;

import com.mogou.dto.CandidatureStatsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "applications-service")
public interface ApplicationClient {

    @GetMapping("/api/candidatures/stats")
    List<CandidatureStatsDto> getAllCandidaturesPourStats(@RequestParam("annee") int annee);
}