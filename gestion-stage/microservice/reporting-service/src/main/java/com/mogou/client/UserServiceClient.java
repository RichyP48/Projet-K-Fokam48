package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserServiceClient {
    
    @GetMapping("/api/admin/users/stats")
    List<Map<String, Object>> getUserStatsByFiliere();
}