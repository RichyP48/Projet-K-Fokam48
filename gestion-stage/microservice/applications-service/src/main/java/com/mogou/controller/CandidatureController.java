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
        try {
            Long currentUserId = userService.getCurrentUserId();
            logger.info("User {} requesting applications for student {}", currentUserId, id);
            
            // SECURITY CHECK: Only the student themselves or their teacher can view their applications
            if (!currentUserId.equals(id) && !isTeacherAuthorizedForStudent(currentUserId, id)) {
                logger.warn("🚨 SECURITY VIOLATION: User {} tried to access applications for student {} without authorization", currentUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Candidature> candidatures = candidatureService.findByEtudiantId(id);
            return ResponseEntity.ok(CandidatureMapper.toDtoList(candidatures));
        } catch (Exception e) {
            logger.error("Error getting applications for student {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    /**
     * Récupère toutes les candidatures pour une offre de stage donnée.
     * Endpoint: GET /api/candidatures/offre/{id}
     * @param id L'identifiant de l'offre.
     * @return Une liste des candidatures pour l'offre.
     */
    @GetMapping("/offre/{id}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByOffre(@PathVariable Long id) {
        try {
            Long currentUserId = userService.getCurrentUserId();
            logger.info("User {} requesting applications for offer {}", currentUserId, id);
            
            // SECURITY CHECK: Verify user owns this offer
            if (!isUserAuthorizedForOffer(id, currentUserId)) {
                logger.warn("🚨 SECURITY VIOLATION: User {} tried to access applications for offer {} which belongs to another company", currentUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Candidature> candidatures = candidatureService.findByOffreId(id);
            return ResponseEntity.ok(CandidatureMapper.toDtoList(candidatures));
        } catch (Exception e) {
            logger.error("Error getting applications for offer {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une candidature par son identifiant.
     * Endpoint: GET /api/candidatures/{id}
     * @param id L'identifiant de la candidature.
     * @return La candidature correspondante.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDto> getCandidatureById(@PathVariable Long id) {
        try {
            Long currentUserId = userService.getCurrentUserId();
            logger.info("User {} requesting application {}", currentUserId, id);
            
            // SECURITY CHECK: Verify user is authorized to view this application
            if (!isUserAuthorizedForApplication(id, currentUserId)) {
                logger.warn("🚨 SECURITY VIOLATION: User {} tried to access application {} without authorization", currentUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Candidature candidature = candidatureService.findById(id);
            return ResponseEntity.ok(CandidatureMapper.toDto(candidature));
        } catch (Exception e) {
            logger.error("Error getting application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * INTERNAL ENDPOINT: Récupère une candidature sans vérification de sécurité (pour appels inter-services)
     * Endpoint: GET /api/candidatures/internal/{id}
     * @param id L'identifiant de la candidature.
     * @return La candidature correspondante.
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<CandidatureDto> getCandidatureByIdInternal(@PathVariable Long id) {
        try {
            logger.info("[INTERNAL] Fetching application {} for inter-service call", id);
            Candidature candidature = candidatureService.findById(id);
            return ResponseEntity.ok(CandidatureMapper.toDto(candidature));
        } catch (Exception e) {
            logger.error("[INTERNAL] Error getting application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            logger.info("Company {} attempting to accept application {}", entrepriseId, id);
            
            // SECURITY CHECK: Verify company owns this application
            if (!isCompanyAuthorizedForApplication(id, entrepriseId)) {
                logger.warn("🚨 SECURITY VIOLATION: Company {} tried to accept application {} which belongs to another company", entrepriseId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
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
            logger.info("Company {} attempting to reject application {} with reason: {}", entrepriseId, id, reason);
            
            // SECURITY CHECK: Verify company owns this application
            if (!isCompanyAuthorizedForApplication(id, entrepriseId)) {
                logger.warn("🚨 SECURITY VIOLATION: Company {} tried to reject application {} which belongs to another company", entrepriseId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
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
    
    @PostMapping("/create-complete-test-scenario")
    public ResponseEntity<Map<String, Object>> createCompleteTestScenario() {
        Map<String, Object> response = new HashMap<>();
        try {
            // First, let's see what offers exist for company 2
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            List<com.mogou.client.OfferDto> company2Offers = offersClient.getOffersByCompanyId(2L);
            
            response.put("company2_offers_found", company2Offers.size());
            response.put("company2_offer_ids", company2Offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList()));
            
            if (!company2Offers.isEmpty()) {
                // Create an application for the first offer
                Long firstOfferId = company2Offers.get(0).getId();
                
                // Check if application already exists
                boolean exists = candidatureRepository.existsByEtudiantIdAndOffreId(1L, firstOfferId);
                if (!exists) {
                    Candidature candidature = Candidature.builder()
                        .etudiantId(1L)
                        .offreId(firstOfferId)
                        .statut(StatutCandidature.POSTULE)
                        .datePostulation(java.time.LocalDateTime.now())
                        .commentaires("Test application for company 2's offer")
                        .build();
                    
                    Candidature saved = candidatureRepository.save(candidature);
                    response.put("application_created", true);
                    response.put("application_id", saved.getId());
                    response.put("for_offer_id", firstOfferId);
                } else {
                    response.put("application_created", false);
                    response.put("reason", "Application already exists");
                }
                
                // Now test if company 2 can see this application
                List<CandidatureDto> company2Apps = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(2L);
                response.put("company2_can_see_applications", company2Apps.size());
                response.put("applications_details", company2Apps.stream()
                    .map(app -> "App " + app.getId() + " for offer " + app.getOfferId())
                    .collect(java.util.stream.Collectors.toList()));
                    
            } else {
                response.put("error", "No offers found for company 2. Create offers first.");
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
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
    public ResponseEntity<List<CandidatureDto>> getCompanyApplications(
            @RequestParam(required = false) Long offerId) {
        try {
            Long entrepriseId = userService.getCurrentUserId();
            logger.info("🏢 Getting applications for company: {} (offerId filter: {})", entrepriseId, offerId);
            
            if (entrepriseId == null) {
                logger.error("❌ Company ID is null - JWT token might be invalid");
                return ResponseEntity.ok(List.of());
            }
            
            List<CandidatureDto> candidatures;
            
            if (offerId != null) {
                // SECURITY CHECK: Verify company owns this offer before filtering
                if (!isUserAuthorizedForOffer(offerId, entrepriseId)) {
                    logger.warn("🚨 SECURITY VIOLATION: Company {} tried to filter by offer {} which they don't own", entrepriseId, offerId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                
                // Get applications for specific offer
                List<Candidature> offerApplications = candidatureService.findByOffreId(offerId);
                candidatures = offerApplications.stream()
                    .map(candidature -> ((CandidatureServiceImpl) candidatureService).enrichCandidatureWithOfferInfo(candidature))
                    .collect(java.util.stream.Collectors.toList());
                logger.info("📝 Found {} applications for offer {}", candidatures.size(), offerId);
            } else {
                // Get all applications for company
                candidatures = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(entrepriseId);
                logger.info("📝 Found {} total applications for company {}", candidatures.size(), entrepriseId);
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
        try {
            Long currentUserId = userService.getCurrentUserId();
            logger.info("User {} requesting applications for company {}", currentUserId, entrepriseId);
            
            // REDIRECT TO SECURE ENDPOINT: Force use of /me endpoint for security
            logger.warn("⚠️ DEPRECATED: Redirecting /entreprise/{} to /entreprise/me for user {}", entrepriseId, currentUserId);
            
            // Use the secure endpoint instead
            return getCompanyApplications(null);
            
        } catch (Exception e) {
            logger.error("Error getting applications for company {}: {}", entrepriseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
    
    @GetMapping("/test-current-problem")
    public ResponseEntity<Map<String, Object>> testCurrentProblem() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate the original problem: Company A seeing Company B's applications
            
            // Get all applications in database
            List<Candidature> allApps = candidatureRepository.findAll();
            result.put("total_applications", allApps.size());
            
            // Test: Can we access applications by offer ID without proper authorization?
            if (!allApps.isEmpty()) {
                Long testOfferId = allApps.get(0).getOffreId();
                List<Candidature> offerApps = candidatureRepository.findByOffreId(testOfferId);
                result.put("test_offer_id", testOfferId);
                result.put("applications_for_offer", offerApps.size());
                result.put("problem_exists", "Anyone can access applications by offer ID without authorization check");
            }
            
            // Show which company owns which applications
            for (Candidature app : allApps) {
                try {
                    com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
                    com.mogou.client.OfferDto offer = offersClient.getOfferById(app.getOffreId());
                    result.put("app_" + app.getId() + "_belongs_to_company", offer.getEntrepriseId());
                } catch (Exception e) {
                    result.put("app_" + app.getId() + "_error", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
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
    
    @GetMapping("/debug/isolation-test")
    public ResponseEntity<Map<String, Object>> testCompanyIsolation() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Test all companies
            for (Long companyId = 1L; companyId <= 3L; companyId++) {
                List<CandidatureDto> apps = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(companyId);
                result.put("company_" + companyId + "_applications", apps.size());
                result.put("company_" + companyId + "_details", apps.stream()
                    .map(app -> "App " + app.getId() + " for offer " + app.getOfferId() + " (" + app.getOfferTitle() + ")")
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            // Show all applications in database
            List<Candidature> allApps = candidatureRepository.findAll();
            result.put("total_applications_in_db", allApps.size());
            result.put("all_applications", allApps.stream()
                .map(app -> "App " + app.getId() + " -> Offer " + app.getOffreId() + " by Student " + app.getEtudiantId())
                .collect(java.util.stream.Collectors.toList()));
                
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/debug/flow-test/{companyId}")
    public ResponseEntity<Map<String, Object>> testApplicationFlow(@PathVariable Long companyId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Step 1: Get offers for this company
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            List<com.mogou.client.OfferDto> offers = offersClient.getOffersByCompanyId(companyId);
            result.put("step1_offers_found", offers.size());
            result.put("step1_offer_ids", offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList()));
            
            // Step 2: Get applications for these offers
            List<Long> offerIds = offers.stream().map(com.mogou.client.OfferDto::getId).collect(java.util.stream.Collectors.toList());
            List<Candidature> applications = candidatureRepository.findByOffreIdIn(offerIds);
            result.put("step2_applications_found", applications.size());
            result.put("step2_application_details", applications.stream()
                .map(app -> "App " + app.getId() + " for offer " + app.getOffreId() + " by student " + app.getEtudiantId())
                .collect(java.util.stream.Collectors.toList()));
            
            // Step 3: Show all applications in database for comparison
            List<Candidature> allApps = candidatureRepository.findAll();
            result.put("step3_all_applications", allApps.stream()
                .map(app -> "App " + app.getId() + " -> Offer " + app.getOffreId() + " by Student " + app.getEtudiantId())
                .collect(java.util.stream.Collectors.toList()));
            
            // Step 4: Check if there's a mismatch
            result.put("diagnosis", applications.isEmpty() && !allApps.isEmpty() ? 
                "PROBLEM: Company has no applications but applications exist in database" : 
                "OK: Flow working correctly");
                
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/test-security-simple")
    public ResponseEntity<Map<String, Object>> testSecuritySimple() {
        Map<String, Object> result = new HashMap<>();
        
        // Test without authentication - should show the problem
        result.put("message", "Testing security without authentication");
        
        // Company 1 applications
        List<CandidatureDto> company1Apps = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(1L);
        result.put("company_1_can_see", company1Apps.size() + " applications");
        
        // Company 2 applications  
        List<CandidatureDto> company2Apps = ((CandidatureServiceImpl) candidatureService).findEnrichedByEntrepriseId(2L);
        result.put("company_2_can_see", company2Apps.size() + " applications");
        
        // Check if there's overlap (the security issue)
        boolean hasOverlap = company1Apps.stream().anyMatch(app1 -> 
            company2Apps.stream().anyMatch(app2 -> app1.getId().equals(app2.getId())));
        
        result.put("security_issue_exists", hasOverlap);
        result.put("explanation", hasOverlap ? "PROBLEM: Same applications visible to different companies" : "OK: Companies see different applications");
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> downloadCV(@PathVariable Long id) {
        try {
            Long currentUserId = userService.getCurrentUserId();
            logger.info("User {} requesting CV download for application {}", currentUserId, id);
            
            // SECURITY CHECK: Verify user is authorized to download this CV
            if (!isUserAuthorizedForApplication(id, currentUserId)) {
                logger.warn("🚨 SECURITY VIOLATION: User {} tried to download CV for application {} without authorization", currentUserId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
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
    
    /**
     * Security method to verify if a company is authorized to modify an application
     */
    private boolean isCompanyAuthorizedForApplication(Long applicationId, Long companyId) {
        try {
            // Get the application
            Candidature candidature = candidatureService.findById(applicationId);
            
            // Get the offer details to find which company owns it
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            com.mogou.client.OfferDto offer = offersClient.getOfferById(candidature.getOffreId());
            
            // Check if the company owns the offer
            boolean authorized = offer.getEntrepriseId().equals(companyId);
            
            logger.info("🔒 Authorization check: Application {} belongs to offer {} owned by company {}. Current company: {}. Authorized: {}", 
                applicationId, candidature.getOffreId(), offer.getEntrepriseId(), companyId, authorized);
            
            return authorized;
        } catch (Exception e) {
            logger.error("❌ Authorization check failed for application {} and company {}: {}", applicationId, companyId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Security method to verify if a user is authorized to view an offer's applications
     */
    private boolean isUserAuthorizedForOffer(Long offerId, Long userId) {
        try {
            // Get the offer details
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            com.mogou.client.OfferDto offer = offersClient.getOfferById(offerId);
            
            // Check if the user owns the offer (company) or is the student who applied
            boolean authorized = offer.getEntrepriseId().equals(userId);
            
            logger.info("🔒 Offer authorization check: Offer {} owned by company {}. Current user: {}. Authorized: {}", 
                offerId, offer.getEntrepriseId(), userId, authorized);
            
            return authorized;
        } catch (Exception e) {
            logger.error("❌ Offer authorization check failed for offer {} and user {}: {}", offerId, userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Security method to verify if a user is authorized to view an application
     */
    private boolean isUserAuthorizedForApplication(Long applicationId, Long userId) {
        try {
            // Get the application
            Candidature candidature = candidatureService.findById(applicationId);
            
            // Check if user is the student who submitted the application
            if (candidature.getEtudiantId().equals(userId)) {
                return true;
            }
            
            // Check if user is the company that owns the offer
            com.mogou.client.OffersClient offersClient = ((CandidatureServiceImpl) candidatureService).getOffersClient();
            com.mogou.client.OfferDto offer = offersClient.getOfferById(candidature.getOffreId());
            
            boolean authorized = offer.getEntrepriseId().equals(userId);
            
            logger.info("🔒 Application authorization check: Application {} by student {} for offer {} owned by company {}. Current user: {}. Authorized: {}", 
                applicationId, candidature.getEtudiantId(), candidature.getOffreId(), offer.getEntrepriseId(), userId, authorized);
            
            return authorized;
        } catch (Exception e) {
            logger.error("❌ Application authorization check failed for application {} and user {}: {}", applicationId, userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Security method to verify if a teacher is authorized to view a student's applications
     */
    private boolean isTeacherAuthorizedForStudent(Long teacherId, Long studentId) {
        try {
            // TODO: Implement proper teacher-student relationship check via user-service
            // For now, we'll implement a basic check - this should be enhanced with actual teacher-student relationships
            
            // Call user-service to verify if teacherId is actually a teacher and has access to studentId
            // This is a placeholder - you should implement the actual teacher-student relationship logic
            
            logger.info("🔒 Teacher authorization check: Teacher {} requesting access to student {}", teacherId, studentId);
            
            // For security, we'll return false by default until proper teacher-student relationships are implemented
            // Only allow if it's an admin or the actual implementation is added
            return false; // Change this when you implement proper teacher-student relationships
            
        } catch (Exception e) {
            logger.error("❌ Teacher authorization check failed for teacher {} and student {}: {}", teacherId, studentId, e.getMessage());
            return false;
        }
    }
}

