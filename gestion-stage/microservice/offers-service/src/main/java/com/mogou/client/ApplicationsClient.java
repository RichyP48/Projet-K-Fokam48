package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "applications-service")
public interface ApplicationsClient {
    
    @GetMapping("/api/candidatures/offre/{offreId}/count")
    Long countApplicationsByOfferId(@PathVariable("offreId") Long offreId);
}