package com.mogou.service;

import com.mogou.client.UserClient;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserClient userClient;
    
    public Long getCurrentUserId() {
        // Pour les tests, retourner l'ID de l'étudiant Johan
        return 2L;
    }
}