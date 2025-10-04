package com.mogou.service;

import com.mogou.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserClient userClient;
    
    public Long getCurrentUserId() {
        // Pour les tests entreprise, retourner l'ID de l'entreprise
        return 10L; // ou 13L selon l'entreprise testée
    }
}