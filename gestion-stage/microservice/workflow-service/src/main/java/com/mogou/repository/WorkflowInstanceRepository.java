package com.mogou.repository;

import com.mogou.model.StageState;
import com.mogou.model.StageWorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'accès aux données de l'entité StageWorkflowInstance.
 * Gère la persistance de l'état actuel de chaque machine à états.
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<StageWorkflowInstance, Long> {
    /**
     * MÉTHODE AJOUTÉE : Trouve les instances de workflow qui sont bloquées.
     * @param state L'état dans lequel le processus est bloqué (ex: CONVENTION_GENEREE).
     * @param since La date limite. On cherche les processus mis à jour AVANT cette date.
     * @return Une liste d'instances de workflow bloquées.
     */
    @Query("SELECT i FROM StageWorkflowInstance i WHERE i.currentState = :state AND i.lastUpdatedAt < :since")
    List<StageWorkflowInstance> findStalledInstances(
            @Param("state") StageState state,
            @Param("since") LocalDateTime since
    );
}