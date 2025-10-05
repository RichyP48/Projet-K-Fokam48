package com.mogou.config;

import com.mogou.model.*;
import com.mogou.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeAcademicStructure();
        initializeUsers();
    }

    private void initializeAcademicStructure() {
        // Créer l'école de Sarah si elle n'existe pas
        School school = schoolRepository.findByName("École Sarah Mogou")
                .orElseGet(() -> {
                    School newSchool = new School();
                    newSchool.setName("École Sarah Mogou");
                    newSchool.setAddress("123 Rue de l'Éducation, Yaoundé");
                    newSchool.setDescription("École dirigée par Sarah Mogou");
                    return schoolRepository.save(newSchool);
                });
        
        // Créer la faculté de Sarah
        Faculty faculty = facultyRepository.findByNameAndSchool("Faculté des Sciences", school)
                .orElseGet(() -> {
                    Faculty newFaculty = new Faculty();
                    newFaculty.setName("Faculté des Sciences");
                    newFaculty.setSchool(school);
                    return facultyRepository.save(newFaculty);
                });
        
        log.info("Structure académique initialisée");
    }
    
    private void initializeUsers() {
        // Admin
        createUserIfNotExists("richardmogou99@gmail.com", "password", Role.ADMIN, "Richard", "Mogou", null, null, null);
        
        // Récupérer les références académiques
        School school = schoolRepository.findByName("École Sarah Mogou").orElse(null);
        Faculty faculty = school != null ? facultyRepository.findByNameAndSchool("Faculté des Sciences", school).orElse(null) : null;
        
        // Créer d'abord Sarah sans département
        createUserIfNotExists("sarahmogou@gmail.com", "password", Role.ENSEIGNANT, "Sarah", "Mogou", school, faculty, null);
        
        // Créer le département avec Sarah comme responsable
        Department department = null;
        if (faculty != null) {
            User sarah = userRepository.findByEmail("sarahmogou@gmail.com").orElse(null);
            department = departmentRepository.findByNameAndFaculty("Informatique", faculty)
                    .orElseGet(() -> {
                        Department newDept = new Department();
                        newDept.setName("Informatique");
                        newDept.setFaculty(faculty);
                        newDept.setHeadTeacher(sarah);
                        return departmentRepository.save(newDept);
                    });
            
            // Assigner le département à Sarah après création
            if (sarah != null && sarah.getUserProfile() != null && department != null) {
                sarah.getUserProfile().setDepartment(department);
                userRepository.save(sarah);
                log.info("Département assigné à Sarah: {}", department.getName());
            }
        }
        
        createUserIfNotExists("johanmogou@gmail.com", "password", Role.ETUDIANT, "Johan", "Mogou", school, faculty, department);
        
        // Ajouter plus d'étudiants dans le département de Sarah
        createUserIfNotExists("marie.dupont@student.com", "password", Role.ETUDIANT, "Marie", "Dupont", school, faculty, department);
        createUserIfNotExists("pierre.martin@student.com", "password", Role.ETUDIANT, "Pierre", "Martin", school, faculty, department);
        createUserIfNotExists("sophie.bernard@student.com", "password", Role.ETUDIANT, "Sophie", "Bernard", school, faculty, department);
        createUserIfNotExists("lucas.petit@student.com", "password", Role.ETUDIANT, "Lucas", "Petit", school, faculty, department);
        createUserIfNotExists("emma.robert@student.com", "password", Role.ETUDIANT, "Emma", "Robert", school, faculty, department);
        
        // École de test
        createUserIfNotExists("test@school.com", "password123", Role.ENSEIGNANT, "Établissement", "École Test", null, null, null);
        
        // Entreprise
        createUserIfNotExists("researchecenter@pkf.com", "password", Role.ENTREPRISE, "Research", "Center PKF", null, null, null);
        
        log.info("Utilisateurs par défaut initialisés");
    }

    private void createUserIfNotExists(String email, String password, Role role, String firstName, String lastName, 
                                      School school, Faculty faculty, Department department) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setActive(true);

            UserProfile profile = new UserProfile();
            profile.setPrenom(firstName);
            profile.setNom(lastName);
            profile.setTelephone(generatePhoneNumber(email));
            profile.setAdresse(generateAddress(role));
            
            // Ajouter les références académiques
            if (role == Role.ETUDIANT && school != null) {
                profile.setSchool(school);
                profile.setFaculty(faculty);
                profile.setDepartment(department);
                profile.setStudyLevel("L3");
            } else if (role == Role.ENSEIGNANT && school != null) {
                profile.setSchool(school);
                profile.setFaculty(faculty);
                if (department != null) {
                    profile.setDepartment(department);
                }
            }
            
            profile.setUser(user);
            user.setUserProfile(profile);

            userRepository.save(user);
            log.info("Utilisateur créé: {} avec le rôle {}", email, role);
        }
    }
    
    private String generatePhoneNumber(String email) {
        // Générer des numéros de téléphone réalistes basés sur l'email
        if (email.contains("richard")) return "+237 6 77 88 99 00";
        if (email.contains("sarah")) return "+237 6 55 66 77 88";
        if (email.contains("johan")) return "+237 6 99 00 11 22";
        if (email.contains("marie")) return "+237 6 44 55 66 77";
        if (email.contains("pierre")) return "+237 6 33 44 55 66";
        if (email.contains("sophie")) return "+237 6 22 33 44 55";
        if (email.contains("lucas")) return "+237 6 11 22 33 44";
        if (email.contains("emma")) return "+237 6 88 99 00 11";
        if (email.contains("test")) return "+237 6 12 34 56 78";
        if (email.contains("research")) return "+237 2 22 33 44 55";
        return "+237 6 00 11 22 33";
    }
    
    private String generateAddress(Role role) {
        // Générer des adresses réalistes selon le rôle
        switch (role) {
            case ADMIN:
                return "123 Avenue de l'Administration, Yaoundé";
            case ENSEIGNANT:
                return "456 Rue des Professeurs, Yaoundé";
            case ETUDIANT:
                return "789 Quartier Universitaire, Yaoundé";
            case ENTREPRISE:
                return "321 Zone Industrielle, Douala";
            default:
                return "Yaoundé, Cameroun";
        }
    }
}