package com.mogou.controller;

import com.mogou.dto.*;
import com.mogou.model.*;
import com.mogou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/schools")
    public List<SchoolDto> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(school -> {
                    SchoolDto dto = new SchoolDto();
                    dto.setId(school.getId());
                    dto.setName(school.getName());
                    dto.setAddress(school.getAddress());
                    dto.setDescription(school.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/faculties/{schoolId}")
    public List<FacultyDto> getFacultiesBySchool(@PathVariable Long schoolId) {
        return facultyRepository.findBySchoolId(schoolId).stream()
                .map(faculty -> {
                    FacultyDto dto = new FacultyDto();
                    dto.setId(faculty.getId());
                    dto.setName(faculty.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/departments/{facultyId}")
    public List<DepartmentDto> getDepartmentsByFaculty(@PathVariable Long facultyId) {
        return departmentRepository.findByFacultyId(facultyId).stream()
                .map(department -> {
                    DepartmentDto dto = new DepartmentDto();
                    dto.setId(department.getId());
                    dto.setName(department.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/dropdowns/schools")
    public List<SchoolDropdown> getSchoolDropdowns() {
        return schoolRepository.findAllSchoolDropdown();
    }

    @GetMapping("/dropdowns/faculties/{schoolId}")
    public List<FacultyDropdown> getFacultyDropdownsBySchool(@PathVariable Long schoolId) {
        return facultyRepository.findFacultyDropdownBySchoolId(schoolId);
    }

    @GetMapping("/dropdowns/departments/{facultyId}")
    public List<DepartmentDropdown> getDepartmentDropdownsByFaculty(@PathVariable Long facultyId) {
        return departmentRepository.findDepartmentDropdownByFacultyId(facultyId);
    }

    @GetMapping("/school-names")
    public List<String> getAllSchoolNames() {
        return schoolRepository.findAll().stream()
                .map(School::getName)
                .collect(Collectors.toList());
    }

    @GetMapping("/faculty-names")
    public List<String> getAllFacultyNames() {
        return facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/department-names")
    public List<String> getAllDepartmentNames() {
        return departmentRepository.findAll().stream()
                .map(Department::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/test")
    public String testConnection() {
        return "Academic service is working! Time: " + java.time.LocalDateTime.now();
    }
}