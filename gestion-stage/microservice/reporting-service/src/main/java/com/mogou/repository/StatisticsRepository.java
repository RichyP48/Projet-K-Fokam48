package com.mogou.repository;

import com.mogou.model.StatistiqueFiliere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<StatistiqueFiliere, Long> {

    List<StatistiqueFiliere> findByPeriodeOrderByNombreEtudiantsDesc(String periode);
    
    @Query("SELECT s FROM StatistiqueFiliere s WHERE s.periode = :periode AND s.dateMaj = (SELECT MAX(s2.dateMaj) FROM StatistiqueFiliere s2 WHERE s2.periode = :periode)")
    List<StatistiqueFiliere> findLatestByPeriode(@Param("periode") String periode);
    
    void deleteByPeriode(String periode);
}