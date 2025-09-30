package com.mogou.controller;

import com.mogou.model.Role;
import com.mogou.model.User;
import com.mogou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final UserRepository userRepository;

    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> isEntrepriseValid(@PathVariable Long id) {
        boolean isValid = userRepository.findById(id)
            .map(user -> user.getRole() == Role.ENTREPRISE && user.isActive())
            .orElse(false);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/{id}/role")
    public ResponseEntity<String> getEntrepriseRole(@PathVariable Long id) {
        String role = userRepository.findById(id)
            .map(user -> user.getRole().name())
            .orElse("NOT_FOUND");
        return ResponseEntity.ok(role);
    }
}