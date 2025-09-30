package com.mogou.repository;

import com.mogou.model.OffreStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OffreStageRepository extends JpaRepository<OffreStage, Long>, JpaSpecificationExecutor<OffreStage> {
    Page<OffreStage> findByEntrepriseId(Long entrepriseId, Pageable pageable);
}