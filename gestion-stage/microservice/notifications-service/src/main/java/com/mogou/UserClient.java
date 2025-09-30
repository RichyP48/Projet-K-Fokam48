package com.mogou;

import com.mogou.dto.UserDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * Appelle le user-service pour récupérer les détails d'un utilisateur par son ID.
     * Cet endpoint doit exister dans le user-service.
     * @param userId L'ID de l'utilisateur.
     * @return Les détails de l'utilisateur.
     */
    @GetMapping("/api/users/{id}/details")
    UserDetailsDto getUserDetailsById(@PathVariable("id") Long userId);
}
