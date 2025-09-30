package com.mogou.service;

import com.mogou.dto.CompanyRegistrationRequest;
import com.mogou.dto.AuthResponse;
import com.mogou.jwt.JwtUtil;
import com.mogou.model.*;
import com.mogou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerCompany(CompanyRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getContactEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User companyUser = new User();
        companyUser.setEmail(request.getContactEmail());
        companyUser.setPassword(passwordEncoder.encode(request.getPassword()));
        companyUser.setRole(Role.ENTREPRISE);
        companyUser.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setNom(request.getCompanyName());
        companyUser.setUserProfile(profile);
        profile.setUser(companyUser);

        userRepository.save(companyUser);

        return new AuthResponse(jwtUtil.generateToken(companyUser));
    }
}