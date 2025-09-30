package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "conventions-service")
public interface ConventionClient {
    @PostMapping("/api/conventions/generate")
    void generateConvention(@RequestBody Map<String, Long> payload); // Ex: {"candidatureId": 123}
}
