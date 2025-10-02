package com.mogou.controller;

import com.mogou.dto.CandidatureDto;
import com.mogou.dto.CandidatureMapper;
import com.mogou.dto.CreateCandidatureRequest;
import com.mogou.dto.StatusUpdateRequest;
import com.mogou.model.Candidature;
import com.mogou.service.CandidatureService;
import com.mogou.service.CandidatureServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final com.mogou.service.UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(CandidatureController.class);
    /**
     * Crée une nouvelle candidature.
     * Endpoint: POST /api/candidatures
     * @param request Les informations pour la création (etudiantId, offreId, etc.).
     * @return La candidature créée avec un statut 201 Created.
     */
     @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CandidatureDto> createCandidature(
            @RequestParam("offreId") Long offreId,
            @RequestParam("commentaires") String commentaires,
            @RequestParam(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cvFile) {
        
        logger.info("Received request to create candidature for offer: {}", offreId);
        logger.info("CV file present: {}", cvFile != null ? cvFile.getOriginalFilename() : "No file");
        try {
            // Récupérer l'ID étudiant depuis le contexte de sécurité (JWT)
            Long etudiantId = userService.getCurrentUserId();
            
            CreateCandidatureRequest request = new CreateCandidatureRequest();
            request.setEtudiantId(etudiantId);
            request.setOffreId(offreId);
            request.setCommentaires(commentaires);
            
            Candidature nouvelleCandidature = candidatureService.create(request);
            
            // Sauvegarder le fichier CV dans MinIO si présent
            if (cvFile != null && !cvFile.isEmpty()) {
                logger.info("Uploading CV file: {} ({})", cvFile.getOriginalFilename(), cvFile.getSize());
                candidatureService.attachDocument(nouvelleCandidature.getId(), cvFile, "cv");
            }
            logger.info("Candidature created successfully: {}", nouvelleCandidature);
            return new ResponseEntity<>(CandidatureMapper.toDto(nouvelleCandidature), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.warn("Candidature already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            logger.error("Error creating candidature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/test-create")
    public ResponseEntity<Map<String, Object>> testCreateCandidature(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test candidature endpoint working");
        response.put("received", data);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère toutes les candidatures pour un étudiant donné.
     * Endpoint: GET /api/candidatures/etudiant/{id}
     * @param id L'identifiant de l'étudiant.
     * @return Une liste des candidatures de l'étudiant.
     */
    @GetMapping("/etudiant/{id}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByEtudiant(@PathVariable Long id) {
        List<Candidature> candidatures = candidatureService.findByEtudiantId(id);
        return ResponseEntity.ok(CandidatureMapper.toDtoList(candidatures));
    }

    /**
     * Récupère toutes les candidatures pour une offre de stage donnée.
     * Endpoint: GET /api/candidatures/offre/{id}
     * @param id L'identifiant de l'offre.
     * @return Une liste des candidatures pour l'offre.
     */
    @GetMapping("/offre/{id}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByOffre(@PathVariable Long id) {
        List<Candidature> candidatures = candidatureService.findByOffreId(id);
        return ResponseEntity.ok(CandidatureMapper.toDtoList(candidatures));
    }

    /**
     * Récupère une candidature par son identifiant.
     * Endpoint: GET /api/candidatures/{id}
     * @param id L'identifiant de la candidature.
     * @return La candidature correspondante.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDto> getCandidatureById(@PathVariable Long id) {
        Candidature candidature = candidatureService.findById(id);
        return ResponseEntity.ok(CandidatureMapper.toDto(candidature));
    }

    /**
     * Met à jour le statut d'une candidature.
     * Endpoint: PUT /api/candidatures/{id}/status
     * @param id L'identifiant de la candidature à modifier.
     * @param request Les informations pour la mise à jour (nouvel événement, commentaire, etc.).
     * @return La candidature mise à jour.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<CandidatureDto> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        Candidature candidatureMiseAJour = candidatureService.changeStatus(id, request.getEvent(), request.getCommentaire(), request.getUserId());
        return ResponseEntity.ok(CandidatureMapper.toDto(candidatureMiseAJour));
    }
    
    @PutMapping("/{id}/accept")
    public ResponseEntity<CandidatureDto> acceptApplication(@PathVariable Long id) {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            logger.info("Company {} accepting application {}", entrepriseId, id);
            
            Candidature candidature = candidatureService.acceptApplication(id, entrepriseId);
            logger.info("Application {} accepted, convention generation triggered", id);
            
            return ResponseEntity.ok(CandidatureMapper.toDto(candidature));
        } catch (Exception e) {
            logger.error("Error accepting application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<CandidatureDto> rejectApplication(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            String reason = body.getOrDefault("reason", "Application rejected by company");
            logger.info("Company {} rejecting application {} with reason: {}", entrepriseId, id, reason);
            
            Candidature candidature = candidatureService.rejectApplication(id, entrepriseId, reason);
            
            return ResponseEntity.ok(CandidatureMapper.toDto(candidature));
        } catch (Exception e) {
            logger.error("Error rejecting application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Applications service is working!");
    }
    

    
    @PostMapping("/test-post")
    public ResponseEntity<String> testPost() {
        return ResponseEntity.ok("POST endpoint is working!");
    }
    
    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> simplePost(@RequestBody(required = false) Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simple POST working");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-user-connection")
    public ResponseEntity<Map<String, Object>> testUserConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test de la connexion avec user-service
            Long currentUserId = userService.getCurrentUserId();
            response.put("status", "success");
            response.put("message", "Connexion avec user-service établie");
            response.put("currentUserId", currentUserId);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur de connexion avec user-service: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/etudiant/me")
    public ResponseEntity<List<CandidatureDto>> getMyApplications() {
        Long etudiantId = userService.getCurrentUserId();
        List<CandidatureDto> candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEtudiantId(etudiantId);
        return ResponseEntity.ok(candidatures);
    }
    
    @GetMapping("/entreprise/me")
    public ResponseEntity<List<CandidatureDto>> getCompanyApplications() {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            logger.info("🏢 Getting applications for company: {}", entrepriseId);
            
            if (entrepriseId == null) {
                logger.error("❌ Company ID is null - JWT token might be invalid");
                return ResponseEntity.ok(List.of());
            }
            
            List<CandidatureDto> candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(entrepriseId);
            logger.info("📝 Found {} applications for company {}", candidatures.size(), entrepriseId);
            
            // Debug: log first few applications
            if (!candidatures.isEmpty()) {
                logger.info("📋 Sample application: {}", candidatures.get(0));
            }
            
            return ResponseEntity.ok(candidatures);
        } catch (Exception e) {
            logger.error("❌ Error getting company applications: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByEntreprise(@PathVariable Long entrepriseId) {
        List<CandidatureDto> candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(entrepriseId);
        return ResponseEntity.ok(candidatures);
    }
    
    @GetMapping("/enseignant/me")
    public ResponseEntity<List<CandidatureDto>> getTeacherStudentApplications() {
        // TODO: Récupérer l'ID enseignant depuis JWT
        // TODO: Récupérer les étudiants de cet enseignant depuis user-service
        // TODO: Récupérer les candidatures de ces étudiants
        Long enseignantId = 3L; // Temporaire
        List<Candidature> candidatures = candidatureService.findByEtudiantId(2L); // Étudiant test
        return ResponseEntity.ok(CandidatureMapper.toDtoList(candidatures));
    }
    
    @GetMapping("/test-offers/{companyId}")
    public ResponseEntity<Map<String, Object>> testOffersConnection(@PathVariable Long companyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            List<com.mogou.client.OfferDto> offers = offersClient.getOffersByCompanyId(companyId);
            result.put("success", true);
            result.put("offersCount", offers.size());
            result.put("offers", offers);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/test-offer/{offerId}")
    public ResponseEntity<Map<String, Object>> testSingleOffer(@PathVariable Long offerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            com.mogou.client.OfferDto offer = offersClient.getOfferById(offerId);
            result.put("success", true);
            result.put("offer", offer);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/offre/{offreId}/count")
    public ResponseEntity<Long> countApplicationsByOfferId(@PathVariable Long offreId) {
        Long count = candidatureService.countByOffreId(offreId);
        return ResponseEntity.ok(count);
    }
}

