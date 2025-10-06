package com.mogou.repository;

import com.mogou.model.SignatureConvention;
import com.mogou.model.TypeSignataire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SignatureRepository extends JpaRepository<SignatureConvention, Long> {
    long countByConvention_Id(Long conventionId);
    List<SignatureConvention> findByConvention_Id(Long conventionId);
    List<SignatureConvention> findByConvention_IdAndTypeSignataire(Long conventionId, TypeSignataire typeSignataire);
}
