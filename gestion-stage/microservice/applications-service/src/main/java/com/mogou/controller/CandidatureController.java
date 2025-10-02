package com.mogou.controller;

import com.mogou.dto.CandidatureDto;
import com.mogou.dto.CandidatureMapper;
import com.mogou.dto.CreateCandidatureRequest;
import com.mogou.dto.StatusUpdateRequest;
import com.mogou.model.Candidature;
import com.mogou.enums.StatutCandidature;
import com.mogou.service.CandidatureService;
import com.mogou.service.CandidatureServiceImpl;
import com.mogou.repository.CandidatureRepository;
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
    private final CandidatureRepository candidatureRepository;
    private static final Logger logger = LoggerFactory.getLogger(CandidatureController.class);
    /**
     * Crée une nouvelle candidature.
     * Endpoint: POST /api/candidatures
     * @param request Les informations pour la création (etudiantId, offreId, etc.).
     * @return La candidature créée avec un statut 201 Created.
     */
     @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createCandidature(
            @RequestParam("offreId") Long offreId,
            @RequestParam("commentaires") String commentaires,
            @RequestParam(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cvFile) {
        
        logger.info("Received request to create candidature for offer: {}", offreId);
        logger.info("CV file present: {}", cvFile != null ? cvFile.getOriginalFilename() : "No file");
        
        // Validation des paramètres
        if (offreId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "L'ID de l'offre est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (commentaires == null || commentaires.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Les commentaires sont requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (cvFile == null || cvFile.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le fichier CV est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            // Récupérer l'ID étudiant depuis le contexte de sécurité (JWT)
            Long etudiantId = userService.getCurrentUserId();
            logger.info("Creating candidature for student: {} and offer: {}", etudiantId, offreId);
            
            if (etudiantId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Impossible de récupérer l'ID de l'étudiant. Veuillez vous reconnecter.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            CreateCandidatureRequest request = new CreateCandidatureRequest();
            request.setEtudiantId(etudiantId);
            request.setOffreId(offreId);
            request.setCommentaires(commentaires);
            
            Candidature nouvelleCandidature = candidatureService.create(request);
            
            // Sauvegarder le fichier CV dans MinIO si présent
            logger.info("Uploading CV file: {} ({})", cvFile.getOriginalFilename(), cvFile.getSize());
            candidatureService.attachDocument(nouvelleCandidature.getId(), cvFile, "cv");
            
            logger.info("Candidature created successfully: {}", nouvelleCandidature.getId());
            return new ResponseEntity<>(CandidatureMapper.toDto(nouvelleCandidature), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.warn("Candidature already exists: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Vous avez déjà postulé pour cette offre");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            logger.error("Error creating candidature: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de l'envoi de la candidature: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/test-create")
    public ResponseEntity<Map<String, Object>> testCreateCandidature(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long etudiantId = Long.valueOf(data.get("etudiantId").toString());
            Long offreId = Long.valueOf(data.get("offreId").toString());
            String commentaires = data.get("commentaires").toString();
            
            Candidature candidature = Candidature.builder()
                .etudiantId(etudiantId)
                .offreId(offreId)
                .statut(StatutCandidature.POSTULE)
                .datePostulation(java.time.LocalDateTime.now())
                .commentaires(commentaires)
                .build();
            
            Candidature saved = candidatureRepository.save(candidature);
            response.put("success", true);
            response.put("candidatureId", saved.getId());
            response.put("message", "Candidature created successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
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
            logger.info("Application {} accepted with status: {}", id, candidature.getStatut());
            
            CandidatureDto dto = CandidatureMapper.toDto(candidature);
            logger.info("Returning DTO with status: {} (English: {})", candidature.getStatut(), dto.getStatus());
            
            return ResponseEntity.ok(dto);
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
            logger.info("Application {} rejected with status: {}", id, candidature.getStatut());
            
            CandidatureDto dto = CandidatureMapper.toDto(candidature);
            logger.info("Returning DTO with status: {} (English: {})", candidature.getStatut(), dto.getStatus());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error rejecting application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Applications service is working!");
    }
    
    @PostMapping("/test-submit")
    public ResponseEntity<Map<String, Object>> testSubmit(
            @RequestParam("offreId") Long offreId,
            @RequestParam("commentaires") String commentaires,
            @RequestParam(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cvFile) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("message", "Test submission endpoint working");
            response.put("offreId", offreId);
            response.put("commentaires", commentaires);
            response.put("cvFile", cvFile != null ? cvFile.getOriginalFilename() : "No file");
            response.put("currentUserId", userService.getCurrentUserId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
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
    
    @PostMapping("/create-test-application")
    public ResponseEntity<Map<String, Object>> createTestApplication() {
        Map<String, Object> response = new HashMap<>();
        try {
            Candidature candidature = Candidature.builder()
                .etudiantId(1L)
                .offreId(12L)
                .statut(StatutCandidature.POSTULE)
                .datePostulation(java.time.LocalDateTime.now())
                .commentaires("Test candidature pour entreprise 2")
                .build();
            
            Candidature saved = candidatureRepository.save(candidature);
            response.put("success", true);
            response.put("candidatureId", saved.getId());
            response.put("message", "Test application created for offer 12");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
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
            
            // Debug: Test the service method directly
            List<Candidature> rawCandidatures = candidatureService.findByEntrepriseId(entrepriseId);
            logger.info("🔍 Raw candidatures found: {}", rawCandidatures.size());
            
            List<CandidatureDto> candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(entrepriseId);
            logger.info("📝 Found {} enriched applications for company {}", candidatures.size(), entrepriseId);
            
            // Debug: log all applications
            for (int i = 0; i < candidatures.size(); i++) {
                logger.info("📋 Application {}: ID={}, OfferTitle={}, StudentName={}", 
                    i+1, candidatures.get(i).getId(), candidatures.get(i).getOfferTitle(), candidatures.get(i).getStudentName());
            }
            
            return ResponseEntity.ok(candidatures);
        } catch (Exception e) {
            logger.error("❌ Error getting company applications: {}", e.getMessage(), e);
            e.printStackTrace();
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
    
    @GetMapping("/debug/company-isolation/{companyId}")
    public ResponseEntity<Map<String, Object>> debugCompanyIsolation(@PathVariable Long companyId) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            
            // Test company 1
            List<com.mogou.client.OfferDto> company1Offers = offersClient.getOffersByCompanyId(1L);
            List<Long> company1OfferIds = company1Offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList());
            List<Candidature> company1Apps = candidatureRepository.findByOffreIdIn(company1OfferIds);
            
            // Test company 2
            List<com.mogou.client.OfferDto> company2Offers = offersClient.getOffersByCompanyId(2L);
            List<Long> company2OfferIds = company2Offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList());
            List<Candidature> company2Apps = candidatureRepository.findByOffreIdIn(company2OfferIds);
            
            // Test requested company
            List<com.mogou.client.OfferDto> requestedOffers = offersClient.getOffersByCompanyId(companyId);
            List<Long> requestedOfferIds = requestedOffers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList());
            List<Candidature> requestedApps = candidatureRepository.findByOffreIdIn(requestedOfferIds);
            
            debug.put("company1_offers", company1OfferIds);
            debug.put("company1_applications", company1Apps.size());
            debug.put("company2_offers", company2OfferIds);
            debug.put("company2_applications", company2Apps.size());
            debug.put("requested_company", companyId);
            debug.put("requested_offers", requestedOfferIds);
            debug.put("requested_applications", requestedApps.size());
            
            // Check if same application appears in multiple companies
            debug.put("isolation_test", "Applications should only appear for their respective companies");
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
    }
    
    @GetMapping("/offre/{offreId}/count")
    public ResponseEntity<Long> countApplicationsByOfferId(@PathVariable Long offreId) {
        Long count = candidatureService.countByOffreId(offreId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/test-status-mapping")
    public ResponseEntity<Map<String, Object>> testStatusMapping() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long entrepriseId = userService.getCurrentUserId();
            result.put("entrepriseId", entrepriseId);
            
            List<CandidatureDto> candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(entrepriseId);
            result.put("totalCandidatures", candidatures.size());
            
            Map<String, Long> statusCounts = new HashMap<>();
            for (CandidatureDto candidature : candidatures) {
                String status = candidature.getStatus();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            result.put("statusCounts", statusCounts);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/debug/all-applications")
    public ResponseEntity<Map<String, Object>> debugAllApplications() {
        Map<String, Object> result = new HashMap<>();
        List<Candidature> allApplications = candidatureRepository.findAll();
        result.put("totalApplications", allApplications.size());
        result.put("applications", allApplications);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            // Test database connection
            long applicationCount = candidatureRepository.count();
            health.put("database", "OK");
            health.put("applicationCount", applicationCount);
            
            // Test user service
            try {
                Long currentUserId = userService.getCurrentUserId();
                health.put("userService", "OK");
                health.put("currentUserId", currentUserId);
            } catch (Exception e) {
                health.put("userService", "ERROR: " + e.getMessage());
            }
            
            // Test offers service
            try {
                com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
                List<com.mogou.client.OfferDto> offers = offersClient.getOffersByCompanyId(1L);
                health.put("offersService", "OK");
                health.put("sampleOffersCount", offers.size());
            } catch (Exception e) {
                health.put("offersService", "ERROR: " + e.getMessage());
            }
            
            // Test file storage
            com.mogou.service.FileStorageService fileService = ((CandidatureServiceImpl) candidatureService).getFileStorageService();
            health.put("minioAvailable", fileService.isMinioAvailable());
            
            health.put("status", "OK");
            health.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
        }
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/debug/direct-test/{companyId}")
    public ResponseEntity<Map<String, Object>> debugDirectTest(@PathVariable Long companyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Candidature> candidatures = candidatureService.findByEntrepriseId(companyId);
            result.put("companyId", companyId);
            result.put("candidaturesFound", candidatures.size());
            result.put("candidatures", candidatures);
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> downloadCV(@PathVariable Long id) {
        try {
            Candidature candidature = candidatureService.findById(id);
            if (candidature.getCvPath() == null || candidature.getCvPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            com.mogou.service.FileStorageService fileService = ((CandidatureServiceImpl) candidatureService).getFileStorageService();
            byte[] cvData = fileService.downloadFile(candidature.getCvPath());
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"CV_" + candidature.getEtudiantId() + ".pdf\"")
                .body(cvData);
        } catch (Exception e) {
            logger.error("Error downloading CV for application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

