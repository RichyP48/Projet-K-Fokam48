package com.mogou.service;

import com.mogou.dto.GenerateConventionRequest;
import com.mogou.dto.SignConventionRequest;
import com.mogou.model.Convention;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ConventionService {
    Convention generate(GenerateConventionRequest request);
    Convention validate(Long conventionId, Long enseignantId);
    Convention sign(Long conventionId, SignConventionRequest request, HttpServletRequest httpServletRequest);
    Convention findById(Long id);
    List<Convention> findByEtudiantId(Long etudiantId);
    List<Convention> findByEnseignantId(Long enseignantId);
    List<Convention> findByEntrepriseId(Long entrepriseId);
    byte[] getConventionPdf(Long id);
    
    // Admin methods
    List<Convention> findAll();
    List<Convention> findPending();
    
    // Test methods
    void testMinioConnection();
}
