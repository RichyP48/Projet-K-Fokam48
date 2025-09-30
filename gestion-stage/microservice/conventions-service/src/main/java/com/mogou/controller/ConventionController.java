package com.mogou.controller;


import com.mogou.dto.GenerateConventionRequest;
import com.mogou.dto.SignConventionRequest;
import com.mogou.model.Convention;
import com.mogou.service.ConventionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conventions")
@RequiredArgsConstructor
public class ConventionController {

    private final ConventionService conventionService;

    @PostMapping("/generate")
    public ResponseEntity<Convention> generateConvention(@Valid @RequestBody GenerateConventionRequest request) {
        return ResponseEntity.ok(conventionService.generate(request));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = conventionService.getConventionPdf(id);
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

