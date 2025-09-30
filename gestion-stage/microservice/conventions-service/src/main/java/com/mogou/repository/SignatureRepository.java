package com.mogou.repository;

import com.mogou.model.SignatureConvention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<SignatureConvention, Long> {
    long countByConvention_Id(Long conventionId);
}
