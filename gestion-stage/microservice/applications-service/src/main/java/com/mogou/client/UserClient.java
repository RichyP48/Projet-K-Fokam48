package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/api/users/{id}/validate")
    boolean isUserValid(@PathVariable("id") Long id);
    
    @GetMapping("/api/users/{id}/role")
    String getUserRole(@PathVariable("id") Long id);
}