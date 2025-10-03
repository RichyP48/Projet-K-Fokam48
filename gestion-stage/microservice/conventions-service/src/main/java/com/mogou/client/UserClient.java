package com.mogou.client;

import com.mogou.dto.UserDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = com.mogou.config.FeignConfig.class)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDetailsDto getUserById(@PathVariable("id") Long id);
}
