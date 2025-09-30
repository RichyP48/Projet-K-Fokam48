package com.mogou.repository;

import com.mogou.model.HistoriqueCandidature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueRepository extends JpaRepository<HistoriqueCandidature, Long> {
}
