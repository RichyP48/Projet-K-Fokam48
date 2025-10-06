package com.mogou.repository;

import com.mogou.model.Convention;
import com.mogou.model.StatutConvention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConventionRepository extends JpaRepository<Convention, Long> {
    List<Convention> findByEtudiantId(Long etudiantId);
    List<Convention> findByEnseignantId(Long enseignantId);
    List<Convention> findByEntrepriseId(Long entrepriseId);
    List<Convention> findByStatut(StatutConvention statut);
    List<Convention> findByCandidatureIdIn(List<Long> candidatureIds);
}