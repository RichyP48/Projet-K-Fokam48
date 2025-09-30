package com.mogou.repository;

import com.mogou.model.RapportPeriodique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'accès aux données de l'entité RapportPeriodique.
 * Gère la persistance des rapports générés automatiquement.
 */
@Repository
public interface RapportPeriodiqueRepository extends JpaRepository<RapportPeriodique, Long> {

    /**
     * Trouve un rapport par sa période de génération.
     * @param periode La période (ex: "2024-09").
     * @return Un Optional contenant le rapport s'il existe.
     */
    Optional<RapportPeriodique> findByPeriode(String periode);
}

