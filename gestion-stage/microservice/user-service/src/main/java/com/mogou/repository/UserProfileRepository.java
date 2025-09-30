package com.mogou.repository;

import com.mogou.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'accès aux données de l'entité UserProfile.
 * Spring Data JPA implémentera automatiquement les méthodes CRUD de base (save, findById, etc.).
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

}