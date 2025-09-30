package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "conventions-service", url = "http://localhost:8095")
public interface ConventionsServiceClient {
    
    @GetMapping("/api/conventions/stats")
    List<Map<String, Object>> getConventionsStats();
}