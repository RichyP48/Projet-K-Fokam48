package com.mogou.repository;

import com.mogou.model.EvaluationTemplate;
import com.mogou.model.TypeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, Long> {
    Optional<EvaluationTemplate> findByFiliereAndTypeEvaluation(String filiere, TypeEvaluation typeEvaluation);
}
