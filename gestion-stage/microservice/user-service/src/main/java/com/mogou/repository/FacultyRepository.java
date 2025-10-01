package com.mogou.repository;

import com.mogou.dto.FacultyDropdown;
import com.mogou.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findBySchoolId(Long schoolId);
    
    @Query("SELECT f.id AS id, f.name AS name FROM Faculty f")
    List<FacultyDropdown> findAllFacultyDropdown();
    
    @Query("SELECT f.id AS id, f.name AS name FROM Faculty f WHERE f.id IN :ids")
    List<FacultyDropdown> findAllFacultyDropdownByIds(List<Long> ids);
    
    @Query("SELECT f.id AS id, f.name AS name FROM Faculty f WHERE f.school.id = :schoolId")
    List<FacultyDropdown> findFacultyDropdownBySchoolId(Long schoolId);
    
    java.util.Optional<Faculty> findByNameAndSchool(String name, com.mogou.model.School school);
}