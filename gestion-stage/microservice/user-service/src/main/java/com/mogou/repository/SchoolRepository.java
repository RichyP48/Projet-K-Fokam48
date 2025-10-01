package com.mogou.repository;

import com.mogou.dto.SchoolDropdown;
import com.mogou.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    
    @Query("SELECT s.id AS id, s.name AS name FROM School s")
    List<SchoolDropdown> findAllSchoolDropdown();
    
    @Query("SELECT s.id AS id, s.name AS name FROM School s WHERE s.id IN :ids")
    List<SchoolDropdown> findAllSchoolDropdownByIds(List<Long> ids);
    
    java.util.Optional<School> findByName(String name);
}