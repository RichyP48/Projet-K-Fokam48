package com.mogou.controller;

import com.mogou.dto.CreateOffreStageRequest;
import com.mogou.dto.OffreStageDto;
import com.mogou.service.OffreStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OffreStageController {

    private final OffreStageService offreStageService;
    private final com.mogou.repository.OffreStageRepository offreStageRepository;

    @PostMapping
    public ResponseEntity<OffreStageDto> createOffre(@Valid @RequestBody CreateOffreStageRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        // Récupérer l'ID entreprise depuis les headers (passé par API Gateway)
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                Long entrepriseId = Long.parseLong(userIdHeader);
                request.setEntrepriseId(entrepriseId);
                System.out.println("🏢 Creating offer for company: " + entrepriseId);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid user ID in header: " + userIdHeader);
            }
        } else {
            System.err.println("❌ No X-User-Id header found, using request entrepriseId");
        }
        System.out.println("📝 Final request entrepriseId: " + request.getEntrepriseId());
        OffreStageDto created = offreStageService.createOffre(request);
        System.out.println("✅ Offer created with ID: " + created.getId() + " for company: " + created.getEntrepriseId());
        return ResponseEntity.ok(created);
    }

   @GetMapping
public ResponseEntity<Page<OffreStageDto>> searchOffres(
        @RequestParam(required = false) String domaine,
        @RequestParam(required = false) Integer duree,
        @RequestParam(required = false) String ville,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        Pageable pageable = PageRequest.of(page, size);
        Page<OffreStageDto> result;
        if (companyId != null) {
            result = offreStageService.getOffresByEntrepriseId(companyId, pageable);
        } else {
            result = offreStageService.searchOffres(domaine, duree, ville, keyword, pageable);
        }
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        // Log l'erreur
        System.err.println("Erreur lors de la recherche des offres: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<OffreStageDto> getOffreById(@PathVariable Long id) {
        return ResponseEntity.ok(offreStageService.getOffreById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffreStageDto> updateOffre(@PathVariable Long id, @Valid @RequestBody com.mogou.dto.UpdateOffreRequest request) {
        return ResponseEntity.ok(offreStageService.updateOffre(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long id) {
        offreStageService.deleteOffre(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        List<Map<String, Object>> offers = new ArrayList<>();
        
        Map<String, Object> offer1 = new HashMap<>();
        offer1.put("id", 1);
        offer1.put("titre", "Stage Développement Web");
        offer1.put("description", "Développement d'applications web");
        offer1.put("lieu", "Paris");
        offer1.put("duree", 6);
        offer1.put("salaire", 800.0);
        offer1.put("statut", "ACTIVE");
        offers.add(offer1);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", offers);
        response.put("totalElements", 1);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-create")
    public ResponseEntity<Map<String, Object>> createTestData() {
        try {
            com.mogou.model.OffreStage offer1 = new com.mogou.model.OffreStage();
            offer1.setTitre("Stage Développement Java");
            offer1.setDescription("Développement d'applications Java Spring Boot");
            offer1.setDomaine(com.mogou.model.DomaineStage.INFORMATIQUE);
            offer1.setDuree(6);
            offer1.setLocalisation("Paris");
            offer1.setEntrepriseId(2L);
            offer1.setStatut(com.mogou.model.StatutOffre.PUBLIEE);
            offer1.setDatePublication(java.time.LocalDate.now());
            offer1.setDateExpiration(java.time.LocalDate.now().plusDays(30));
            offer1.setSalaire(1000.0);
            
            com.mogou.model.OffreStage offer2 = new com.mogou.model.OffreStage();
            offer2.setTitre("Stage Marketing Digital");
            offer2.setDescription("Gestion des campagnes marketing");
            offer2.setDomaine(com.mogou.model.DomaineStage.MARKETING);
            offer2.setDuree(4);
            offer2.setLocalisation("Lyon");
            offer2.setEntrepriseId(2L);
            offer2.setStatut(com.mogou.model.StatutOffre.PUBLIEE);
            offer2.setDatePublication(java.time.LocalDate.now());
            offer2.setDateExpiration(java.time.LocalDate.now().plusDays(30));
            offer2.setSalaire(800.0);
            
            com.mogou.model.OffreStage saved1 = offreStageRepository.save(offer1);
            com.mogou.model.OffreStage saved2 = offreStageRepository.save(offer2);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Test offers created for company 2");
            result.put("count", 2);
            result.put("offerIds", List.of(saved1.getId(), saved2.getId()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> getSimpleOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> offers = new ArrayList<>();
        
        Map<String, Object> offer1 = new HashMap<>();
        offer1.put("id", 1);
        offer1.put("titre", "Stage Développement Web");
        offer1.put("description", "Développement d'applications web");
        offer1.put("lieu", "Paris");
        offer1.put("duree", 6);
        offer1.put("salaire", 800.0);
        offer1.put("statut", "ACTIVE");
        offers.add(offer1);
        
        Map<String, Object> offer2 = new HashMap<>();
        offer2.put("id", 2);
        offer2.put("titre", "Stage Marketing Digital");
        offer2.put("description", "Gestion des campagnes marketing");
        offer2.put("lieu", "Lyon");
        offer2.put("duree", 4);
        offer2.put("salaire", 600.0);
        offer2.put("statut", "ACTIVE");
        offers.add(offer2);
        
        // Simuler une réponse paginée
        Map<String, Object> response = new HashMap<>();
        response.put("content", offers);
        response.put("totalElements", offers.size());
        response.put("totalPages", 1);
        response.put("number", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }
    

    
    @PostMapping("/test-create")
    public ResponseEntity<Map<String, Object>> testCreate(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test create endpoint working");
        response.put("received", data);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> isOfferValid(@PathVariable Long id) {
        try {
            OffreStageDto offer = offreStageService.getOffreById(id);
            return ResponseEntity.ok(offer != null);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
    
    @GetMapping("/company-data")
    public ResponseEntity<Page<OffreStageDto>> getCompanyData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long entrepriseId = 1L; // TODO: Récupérer depuis JWT
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            Page<OffreStageDto> result = offreStageService.getOffresByEntrepriseId(entrepriseId, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<OffreStageDto>> getOffersByCompanyId(@PathVariable Long companyId) {
        try {
            System.out.println("🔍 Offers-service: Looking for offers for company " + companyId);
            Page<OffreStageDto> result = offreStageService.getOffresByEntrepriseId(companyId, PageRequest.of(0, 1000));
            System.out.println("📋 Offers-service: Found " + result.getContent().size() + " offers for company " + companyId);
            return ResponseEntity.ok(result.getContent());
        } catch (Exception e) {
            System.err.println("❌ Offers-service: Error getting offers for company " + companyId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "offers-service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
    
    @PostMapping("/debug-create")
    public ResponseEntity<Map<String, Object>> debugCreate(@RequestBody CreateOffreStageRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("userIdHeader", httpRequest.getHeader("X-User-Id"));
        debug.put("authHeader", httpRequest.getHeader("Authorization"));
        debug.put("requestEntrepriseId", request.getEntrepriseId());
        debug.put("allHeaders", java.util.Collections.list(httpRequest.getHeaderNames()));
        return ResponseEntity.ok(debug);
    }
    
    @GetMapping("/test/create-offers")
    public ResponseEntity<Map<String, Object>> createTestOffers() {
        try {
            com.mogou.model.OffreStage offer1 = new com.mogou.model.OffreStage();
            offer1.setTitre("Stage Développement Java");
            offer1.setDescription("Développement d'applications Java Spring Boot");
            offer1.setDomaine(com.mogou.model.DomaineStage.INFORMATIQUE);
            offer1.setDuree(6);
            offer1.setLocalisation("Paris");
            offer1.setEntrepriseId(2L);
            offer1.setStatut(com.mogou.model.StatutOffre.PUBLIEE);
            offer1.setDatePublication(java.time.LocalDate.now());
            offer1.setDateExpiration(java.time.LocalDate.now().plusDays(30));
            offer1.setSalaire(1000.0);
            
            com.mogou.model.OffreStage offer2 = new com.mogou.model.OffreStage();
            offer2.setTitre("Stage Marketing Digital");
            offer2.setDescription("Gestion des campagnes marketing");
            offer2.setDomaine(com.mogou.model.DomaineStage.MARKETING);
            offer2.setDuree(4);
            offer2.setLocalisation("Lyon");
            offer2.setEntrepriseId(2L);
            offer2.setStatut(com.mogou.model.StatutOffre.PUBLIEE);
            offer2.setDatePublication(java.time.LocalDate.now());
            offer2.setDateExpiration(java.time.LocalDate.now().plusDays(30));
            offer2.setSalaire(800.0);
            
            com.mogou.model.OffreStage saved1 = offreStageRepository.save(offer1);
            com.mogou.model.OffreStage saved2 = offreStageRepository.save(offer2);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Test offers created for company 2");
            result.put("count", 2);
            result.put("offerIds", List.of(saved1.getId(), saved2.getId()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }
    


}