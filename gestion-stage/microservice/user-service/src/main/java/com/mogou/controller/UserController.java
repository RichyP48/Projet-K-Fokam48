package com.mogou.controller;


import com.mogou.dto.UserProfileDto;
import com.mogou.service.UserProfileService;
import com.mogou.repository.UserRepository;
import com.mogou.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // N'importe quel utilisateur authentifié peut voir son propre profil
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Principal principal) {
        // 'Principal' contient l'utilisateur authentifié (son email dans notre cas)
        return ResponseEntity.ok(userProfileService.getUserProfileByEmail(principal.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(Principal principal, @Valid @RequestBody UserProfileDto profileDto) {
        return ResponseEntity.ok(userProfileService.updateUserProfileByEmail(principal.getName(), profileDto));
    }
    
    // Endpoints spécifiques pour les enseignants
    @GetMapping("/faculty/students")
    @PreAuthorize("hasAuthority('ENSEIGNANT')")
    public ResponseEntity<java.util.List<Map<String, Object>>> getStudentsInMyDepartment(Principal principal, Authentication auth) {
        log.info("👨‍🏫 Faculty students request - User: {}, Authorities: {}", principal.getName(), auth.getAuthorities());
        return ResponseEntity.ok(userProfileService.getStudentsByTeacherDepartment(principal.getName()));
    }
    
    @GetMapping("/faculty/companies")
    @PreAuthorize("hasAuthority('ENSEIGNANT')")
    public ResponseEntity<java.util.List<Map<String, Object>>> getCompaniesForMyStudents(Principal principal) {
        return ResponseEntity.ok(userProfileService.getCompaniesForTeacherStudents(principal.getName()));
    }
    
    @GetMapping("/company/offers")
    @PreAuthorize("hasAuthority('ENTREPRISE')")
    public ResponseEntity<java.util.List<Map<String, Object>>> getMyCompanyOffers(Principal principal) {
        return ResponseEntity.ok(userProfileService.getOffersByCompanyEmail(principal.getName()));
    }
    
    @GetMapping("/company/applications")
    @PreAuthorize("hasAuthority('ENTREPRISE')")
    public ResponseEntity<java.util.List<Map<String, Object>>> getMyCompanyApplications(Principal principal) {
        return ResponseEntity.ok(userProfileService.getApplicationsByCompanyEmail(principal.getName()));
    }
    
    @GetMapping("/faculty/test")
    @PreAuthorize("hasAuthority('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> testFacultyAccess(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Accès autorisé pour l'enseignant");
        response.put("email", principal.getName());
        response.put("timestamp", System.currentTimeMillis());
        
        // Debug info
        User teacher = userRepository.findByEmail(principal.getName()).orElse(null);
        if (teacher != null) {
            response.put("teacherId", teacher.getId());
            response.put("hasProfile", teacher.getUserProfile() != null);
            if (teacher.getUserProfile() != null) {
                response.put("hasDepartment", teacher.getUserProfile().getDepartment() != null);
                if (teacher.getUserProfile().getDepartment() != null) {
                    response.put("departmentId", teacher.getUserProfile().getDepartment().getId());
                    response.put("departmentName", teacher.getUserProfile().getDepartment().getName());
                }
            }
        }
        
        // Count companies
        long companiesCount = userRepository.findByRole(com.mogou.model.Role.ENTREPRISE).size();
        response.put("totalCompanies", companiesCount);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug/auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> debugAuth(Principal principal, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("principal", principal.getName());
        response.put("authorities", auth.getAuthorities().toString());
        response.put("authenticated", auth.isAuthenticated());
        response.put("details", auth.getDetails());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> isUserValid(@PathVariable Long id) {
        boolean isValid = userRepository.findById(id)
            .map(User::isActive)
            .orElse(false);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/{id}/role")
    public ResponseEntity<String> getUserRole(@PathVariable Long id) {
        String role = userRepository.findById(id)
            .map(user -> user.getRole().name())
            .orElse("NOT_FOUND");
        return ResponseEntity.ok(role);
    }
}
