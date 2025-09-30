package com.mogou.service;

import com.mogou.dto.SchoolRegistrationRequest;
import com.mogou.dto.SchoolRegistrationDepartmentDto;
import com.mogou.dto.AuthResponse;
import com.mogou.jwt.JwtUtil;
import com.mogou.model.*;
import com.mogou.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchoolRegistrationService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerSchool(SchoolRegistrationRequest request) {
        System.out.println("🏫 Received school registration request: " + request);
        
        // Vérification unicité email
        if (userRepository.existsByEmail(request.getContactEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        // Validation des facultés
        if (request.getFaculties() == null || request.getFaculties().isEmpty()) {
            throw new RuntimeException("Au moins une faculté est requise");
        }

        // Création utilisateur école
        User schoolUser = new User();
        schoolUser.setEmail(request.getContactEmail());
        schoolUser.setPassword(passwordEncoder.encode(request.getPassword()));
        schoolUser.setRole(Role.ENSEIGNANT);
        schoolUser.setActive(true);

        // Profil école
        UserProfile profile = new UserProfile();
        profile.setNom(request.getSchoolName());
        schoolUser.setUserProfile(profile);
        profile.setUser(schoolUser);

        userRepository.save(schoolUser);

        // Création école
        School school = new School();
        school.setName(request.getSchoolName());
        school.setAddress(request.getSchoolAddress());
        school.setDescription(request.getSchoolDescription());
        schoolRepository.save(school);

        // Création facultés et départements
        request.getFaculties().forEach(facultyDto -> {
            System.out.println("🏫 Creating faculty: " + facultyDto.getName());
            
            Faculty faculty = new Faculty();
            faculty.setName(facultyDto.getName());
            faculty.setSchool(school);
            facultyRepository.save(faculty);

            if (facultyDto.getDepartments() != null && !facultyDto.getDepartments().isEmpty()) {
                facultyDto.getDepartments().forEach(deptDto -> {
                    System.out.println("🏫 Creating department: " + deptDto.getName());
                    
                    // Création enseignant responsable
                    User headTeacher = createHeadTeacher(deptDto);
                    
                    // Création département
                    Department department = new Department();
                    department.setName(deptDto.getName());
                    department.setFaculty(faculty);
                    department.setHeadTeacher(headTeacher);
                    departmentRepository.save(department);
                });
            }
        });

        return new AuthResponse(jwtUtil.generateToken(schoolUser));
    }

    private User createHeadTeacher(SchoolRegistrationDepartmentDto deptDto) {
        User teacher = new User();
        //
        // SECURITY: Do not use hardcoded passwords. Generate a secure random one.
        // In a real-world scenario, this password should be sent to the user
        // via a secure channel (e.g., email) and they should be forced to change it on first login.
        String generatedPassword = RandomStringUtils.randomAlphanumeric(12);
        teacher.setEmail(deptDto.getHeadTeacherEmail());
        teacher.setPassword(passwordEncoder.encode(generatedPassword));
        teacher.setRole(Role.ENSEIGNANT);
        teacher.setActive(true);

        UserProfile teacherProfile = new UserProfile();
        teacherProfile.setNom(deptDto.getHeadTeacherLastName());
        teacherProfile.setPrenom(deptDto.getHeadTeacherFirstName());
        teacher.setUserProfile(teacherProfile);
        teacherProfile.setUser(teacher);

        return userRepository.save(teacher);
    }
}