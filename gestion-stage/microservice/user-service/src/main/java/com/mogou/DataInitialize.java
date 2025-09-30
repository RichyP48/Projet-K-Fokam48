package com.mogou;

import com.mogou.model.Role;
import com.mogou.model.User;
import com.mogou.model.UserProfile;
import com.mogou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialize implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createUser("richardmogou99@gmail.com", "password", Role.ADMIN, "Richard", "Mogou");
        createUser("johanmogou@gmail.com", "password", Role.ETUDIANT, "Johan", "Mogou");
        createUser("sarahmogou@gmail.com", "password", Role.ENSEIGNANT, "Sarah", "Mogou");
        createUser("researchecenter@pkf.com", "password", Role.ENTREPRISE, "Research", "Center PKF");
    }

    private void createUser(String email, String password, Role role, String firstName, String lastName) {
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
        }
    }
}