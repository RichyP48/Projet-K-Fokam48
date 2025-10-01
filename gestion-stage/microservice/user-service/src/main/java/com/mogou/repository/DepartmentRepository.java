package com.mogou.repository;

import com.mogou.dto.DepartmentDropdown;
import com.mogou.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByFacultyId(Long facultyId);
    
    @Query("SELECT d.id AS id, d.name AS name FROM Department d")
    List<DepartmentDropdown> findAllDepartmentDropdown();
    
    @Query("SELECT d.id AS id, d.name AS name FROM Department d WHERE d.id IN :ids")
    List<DepartmentDropdown> findAllDepartmentDropdownByIds(List<Long> ids);
    
    @Query("SELECT d.id AS id, d.name AS name FROM Department d WHERE d.faculty.id = :facultyId")
    List<DepartmentDropdown> findDepartmentDropdownByFacultyId(Long facultyId);
    
    java.util.Optional<Department> findByNameAndFaculty(String name, com.mogou.model.Faculty faculty);
}