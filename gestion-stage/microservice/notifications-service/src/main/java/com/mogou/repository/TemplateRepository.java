package com.mogou.repository;


import com.mogou.model.TemplateNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateNotification, Long> {
    Optional<TemplateNotification> findByType(String type);
}
