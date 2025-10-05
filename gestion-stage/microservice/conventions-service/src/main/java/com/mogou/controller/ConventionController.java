package com.mogou.controller;


import com.mogou.dto.GenerateConventionRequest;
import com.mogou.dto.SignConventionRequest;
import com.mogou.model.Convention;
import com.mogou.service.ConventionService;
import com.mogou.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conventions")
@RequiredArgsConstructor
public class ConventionController {

    private final ConventionService conventionService;
    private final UserService userService;
    private final com.mogou.repository.ConventionRepository conventionRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ENTREPRISE')")
    public ResponseEntity<Convention> generateConvention(@Valid @RequestBody GenerateConventionRequest request) {
        return ResponseEntity.ok(conventionService.generate(request));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = conventionService.getConventionPdf(id);
        if (pdf.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"convention_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<Convention> validateConvention(@PathVariable Long id, @RequestParam Long enseignantId) {
        return ResponseEntity.ok(conventionService.validate(id, enseignantId));
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<Convention> signConvention(@PathVariable Long id, @Valid @RequestBody SignConventionRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(conventionService.sign(id, request, httpServletRequest));
    }

    @GetMapping("/etudiant/{id}")
    public ResponseEntity<List<Convention>> getConventionsByEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.findByEtudiantId(id));
    }

    @GetMapping("/enseignant/{id}")
    public ResponseEntity<List<Convention>> getConventionsByEnseignant(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.findByEnseignantId(id));
    }
    
    @GetMapping("/entreprise/{id}")
    public ResponseEntity<List<Convention>> getConventionsByEntreprise(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.findByEntrepriseId(id));
    }
    
    @GetMapping("/entreprise")
    public ResponseEntity<List<com.mogou.dto.ConventionResponseDto>> getMyCompanyConventions() {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            System.out.println("[CONVENTIONS] Getting conventions for company ID: " + entrepriseId);
            if (entrepriseId != null) {
                List<Convention> conventions = conventionService.findByEntrepriseId(entrepriseId);
                System.out.println("[CONVENTIONS] Found " + conventions.size() + " conventions");
                
                // Convert to DTOs with enriched data
                List<com.mogou.dto.ConventionResponseDto> dtos = conventions.stream()
                    .map(this::enrichConvention)
                    .collect(java.util.stream.Collectors.toList());
                
                System.out.println("[CONVENTIONS] Returning enriched DTOs: " + dtos.size());
                System.out.println("[CONVENTIONS] DTOs content: " + dtos);
                return ResponseEntity.ok(dtos);
            }
            System.out.println("[CONVENTIONS] No company ID found in JWT");
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            System.out.println("[CONVENTIONS] Error getting conventions: " + e.getMessage());
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

    // ===== ADMIN ENDPOINTS =====
    @GetMapping
    public ResponseEntity<List<Convention>> getAllConventions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(conventionService.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Convention>> getPendingConventions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(conventionService.findPending());
    }

    @GetMapping("/admin/agreements")
    public ResponseEntity<List<Convention>> getAdminAgreements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(conventionService.findAll());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Conventions service is running!");
    }
    
    @GetMapping("/test/auth")
    public ResponseEntity<String> testAuth() {
        try {
            Long userId = userService.getCurrentUserId();
            String role = userService.getCurrentUserRole();
            return ResponseEntity.ok("Auth test - User ID: " + userId + ", Role: " + role);
        } catch (Exception e) {
            return ResponseEntity.ok("Auth test failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/test/create-sample")
    public ResponseEntity<String> createSampleConventions() {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            if (entrepriseId == null) {
                return ResponseEntity.ok("Error: No company ID found in JWT token");
            }
            
            // Create sample conventions for candidature IDs 8, 10, 13
            Long[] candidatureIds = {8L, 10L, 13L};
            int created = 0;
            
            for (Long candidatureId : candidatureIds) {
                // Check if convention already exists
                List<Convention> existing = conventionRepository.findByCandidatureIdIn(List.of(candidatureId));
                if (existing.isEmpty()) {
                    Convention convention = new Convention();
                    convention.setCandidatureId(candidatureId);
                    convention.setEtudiantId(1L);
                    convention.setEnseignantId(1L);
                    convention.setEntrepriseId(entrepriseId);
                    convention.setStatut(com.mogou.model.StatutConvention.EN_ATTENTE_VALIDATION);
                    convention.setDateCreation(java.time.LocalDateTime.now());
                    convention.setDocumentPath("test/convention_" + candidatureId + ".pdf");
                    
                    conventionRepository.save(convention);
                    created++;
                }
            }
            
            return ResponseEntity.ok("Created " + created + " sample conventions for company " + entrepriseId);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/test/minio")
    public ResponseEntity<String> testMinio() {
        try {
            conventionService.testMinioConnection();
            return ResponseEntity.ok("MinIO connection successful!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MinIO connection failed: " + e.getMessage());
        }
    }
}

