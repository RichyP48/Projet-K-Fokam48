package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "conventions-service", configuration = com.mogou.config.FeignConfig.class)
public interface ConventionsClient {
    
    @PostMapping("/api/conventions/generate")
    ConventionDto generateConvention(@RequestBody GenerateConventionRequest request);
    
    // DTOs
    class ConventionDto {
        private Long id;
        private String statut;
        // getters/setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
    }
    
    class GenerateConventionRequest {
        private Long candidatureId;
        private Long etudiantId;
        private Long entrepriseId;
        private Long offreId;
        private String titre;
        private Integer duree;
        private String dateDebut;
        
        // getters/setters
        public Long getCandidatureId() { return candidatureId; }
        public void setCandidatureId(Long candidatureId) { this.candidatureId = candidatureId; }
        public Long getEtudiantId() { return etudiantId; }
        public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
        public Long getEntrepriseId() { return entrepriseId; }
        public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }
        public Long getOffreId() { return offreId; }
        public void setOffreId(Long offreId) { this.offreId = offreId; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public Integer getDuree() { return duree; }
        public void setDuree(Integer duree) { this.duree = duree; }
        public String getDateDebut() { return dateDebut; }
        public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    }
}