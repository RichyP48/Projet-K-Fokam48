package com.mogou.client;

import com.mogou.dto.CandidatureStatusDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "applications-service")
public interface ApplicationClient {
    @GetMapping("/api/candidatures/{id}/status")
    CandidatureStatusDto getCandidatureStatus(@PathVariable("id") Long candidatureId);
}