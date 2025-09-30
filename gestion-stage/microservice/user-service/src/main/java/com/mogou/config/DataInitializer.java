package com.mogou.config;

import com.mogou.model.Role;
import com.mogou.model.User;
import com.mogou.model.UserProfile;
import com.mogou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        // Admin
        createUserIfNotExists("richardmogou99@gmail.com", "password", Role.ADMIN, "Richard", "Mogou");
        
        // Étudiant
        createUserIfNotExists("johanmogou@gmail.com", "password", Role.ETUDIANT, "Johan", "Mogou");
        
        // Enseignant
        createUserIfNotExists("sarahmogou@gmail.com", "password", Role.ENSEIGNANT, "Sarah", "Mogou");
        
        // Entreprise
        createUserIfNotExists("researchecenter@pkf.com", "password", Role.ENTREPRISE, "Research", "Center PKF");
        
        log.info("Utilisateurs par défaut initialisés");
    }

    private void createUserIfNotExists(String email, String password, Role role, String firstName, String lastName) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setActive(true);

            UserProfile profile = new UserProfile();
            profile.setPrenom(firstName);
            profile.setNom(lastName);
            profile.setUser(user);
            user.setUserProfile(profile);

            userRepository.save(user);
            log.info("Utilisateur créé: {} avec le rôle {}", email, role);
        }
    }
}