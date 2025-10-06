package com.mogou.controller;

import com.mogou.dto.GenerateConventionRequest;
import com.mogou.model.Convention;
import com.mogou.service.ConventionService;
import com.mogou.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final ConventionService conventionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Convention> generateAgreement(@Valid @RequestBody GenerateConventionRequest request) {
        try {
            System.out.println("[AGREEMENTS] ========== POST /api/agreements received ==========");
            
            // Check authentication
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            System.out.println("[AGREEMENTS] Authentication present: " + (auth != null));
            if (auth != null) {
                System.out.println("[AGREEMENTS] Principal: " + auth.getPrincipal());
                System.out.println("[AGREEMENTS] Authorities: " + auth.getAuthorities());
            }
            
            Long userId = userService.getCurrentUserId();
            String role = userService.getCurrentUserRole();
            System.out.println("[AGREEMENTS] User ID: " + userId + ", Role: " + role);
            System.out.println("[AGREEMENTS] Request data: " + request);
            
            Convention convention = conventionService.generate(request);
            System.out.println("[AGREEMENTS] Convention generated successfully: " + convention.getId());
            return ResponseEntity.ok(convention);
        } catch (Exception e) {
            System.out.println("[AGREEMENTS] Error generating agreement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<com.mogou.dto.ConventionResponseDto>> getMyCompanyAgreements() {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            System.out.println("[AGREEMENTS] Getting agreements for company ID: " + entrepriseId);
            if (entrepriseId != null) {
                List<Convention> conventions = conventionService.findByEntrepriseId(entrepriseId);
                System.out.println("[AGREEMENTS] Found " + conventions.size() + " agreements");
                
                // Convert to DTOs with enriched data
                List<com.mogou.dto.ConventionResponseDto> dtos = conventions.stream()
                    .map(this::enrichConvention)
                    .collect(java.util.stream.Collectors.toList());
                
                System.out.println("[AGREEMENTS] Returning enriched DTOs: " + dtos.size());
                return ResponseEntity.ok(dtos);
            }
            System.out.println("[AGREEMENTS] No company ID found in JWT");
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            System.out.println("[AGREEMENTS] Error getting agreements: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    private com.mogou.dto.ConventionResponseDto enrichConvention(Convention convention) {
        com.mogou.dto.ConventionResponseDto dto = new com.mogou.dto.ConventionResponseDto();
        dto.setId(convention.getId());
        dto.setOfferTitle("Stage pour candidature " + convention.getCandidatureId());
        dto.setStudentName("Étudiant " + convention.getEtudiantId());
        
        // Map French status to English
        String englishStatus = mapStatusToEnglish(convention.getStatut().toString());
        dto.setStatus(englishStatus);
        
        // Set signature/approval status based on convention status
        dto.setSignedByStudent(false); // TODO: Get from signatures table
        dto.setSignedByCompany(false); // TODO: Get from signatures table
        dto.setSignedByFaculty(convention.getDateValidation() != null);
        dto.setApprovedByAdmin("APPROVED".equals(englishStatus));
        
        return dto;
    }
    
    private String mapStatusToEnglish(String frenchStatus) {
        switch (frenchStatus) {
            case "EN_ATTENTE_VALIDATION": return "PENDING_FACULTY_VALIDATION";
            case "VALIDEE": return "PENDING_ADMIN_APPROVAL";
            case "SIGNEE": return "SIGNED";
            case "FINALISEE": return "APPROVED";
            default: return frenchStatus;
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Agreements endpoint is working!");
    }
}