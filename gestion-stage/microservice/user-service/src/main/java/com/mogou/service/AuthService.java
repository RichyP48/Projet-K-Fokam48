package com.mogou.service;


import com.mogou.dto.AuthResponse;
import com.mogou.dto.LoginRequest;
import com.mogou.dto.RegisterRequest;
import com.mogou.jwt.JwtUtil;
import com.mogou.model.User;
import com.mogou.model.UserProfile;
import com.mogou.model.Role;
import com.mogou.repository.UserRepository;
import com.mogou.repository.SchoolRepository;
import com.mogou.repository.FacultyRepository;
import com.mogou.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Cette méthode gère les étudiants et enseignants
        if (request.getRole() == Role.ENTREPRISE) {
            throw new RuntimeException("Utilisez l'endpoint spécifique pour l'enregistrement des entreprises");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Erreur : L'adresse email est déjà utilisée !");
        }

        // Création de l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        // Création du profil associé
        UserProfile profile = new UserProfile();
        profile.setNom(request.getProfile().getNom());
        profile.setPrenom(request.getProfile().getPrenom());
        profile.setTelephone(request.getProfile().getTelephone());
        profile.setAdresse(request.getProfile().getAdresse());
        profile.setStudyLevel(request.getProfile().getStudyLevel());
        
        // Assignation des références académiques pour les étudiants et enseignants
        if (request.getRole() == Role.ETUDIANT || request.getRole() == Role.ENSEIGNANT) {
            // Gérer la création d'école si nécessaire
            if (request.getProfile().getSchoolId() != null) {
                profile.setSchool(schoolRepository.findById(request.getProfile().getSchoolId()).orElse(null));
            } else if (request.getProfile().getNewSchoolName() != null && !request.getProfile().getNewSchoolName().isEmpty()) {
                // Créer nouvelle école
                var newSchool = new com.mogou.model.School();
                newSchool.setName(request.getProfile().getNewSchoolName());
                newSchool.setAddress(request.getProfile().getNewSchoolAddress());
                profile.setSchool(schoolRepository.save(newSchool));
            }
            
            // Gérer la création de faculté si nécessaire
            if (request.getProfile().getFacultyId() != null) {
                profile.setFaculty(facultyRepository.findById(request.getProfile().getFacultyId()).orElse(null));
            } else if (request.getProfile().getNewFacultyName() != null && !request.getProfile().getNewFacultyName().isEmpty()) {
                // Créer nouvelle faculté
                var newFaculty = new com.mogou.model.Faculty();
                newFaculty.setName(request.getProfile().getNewFacultyName());
                newFaculty.setSchool(profile.getSchool());
                profile.setFaculty(facultyRepository.save(newFaculty));
            }
            
            // Gérer la création de département si nécessaire
            if (request.getProfile().getDepartmentId() != null) {
                profile.setDepartment(departmentRepository.findById(request.getProfile().getDepartmentId()).orElse(null));
            } else if (request.getProfile().getNewDepartmentName() != null && !request.getProfile().getNewDepartmentName().isEmpty()) {
                // Créer nouveau département
                var newDepartment = new com.mogou.model.Department();
                newDepartment.setName(request.getProfile().getNewDepartmentName());
                newDepartment.setFaculty(profile.getFaculty());
                newDepartment.setHeadTeacher(user); // L'enseignant devient responsable du département
                profile.setDepartment(departmentRepository.save(newDepartment));
            }
        }
        
        profile.setSpecificAttributes(request.getProfile().getSpecificAttributes());

        // Lier le profil à l'utilisateur
        user.setUserProfile(profile);
        profile.setUser(user);

        userRepository.save(user);

        // Génération du token JWT
        return new AuthResponse(jwtUtil.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            System.out.println("🔐 Attempting login for: '" + request.getEmail() + "'");
            System.out.println("🔍 Email length: " + request.getEmail().length());
            System.out.println("🔍 Email bytes: " + java.util.Arrays.toString(request.getEmail().getBytes()));
            
            // Check if user exists first
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        System.out.println("❌ No user found with email: '" + request.getEmail() + "'");
                        return new RuntimeException("Utilisateur non trouvé");
                    });
            
            System.out.println("✅ User found: " + user.getEmail() + ", Role: " + user.getRole() + ", Active: " + user.isActive());
            System.out.println("🔍 User details: isEnabled=" + user.isEnabled() + ", isAccountNonLocked=" + user.isAccountNonLocked());
            
            // Test password manually
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("🔑 Password matches: " + passwordMatches);
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            System.out.println("✅ Authentication successful");
            return new AuthResponse(jwtUtil.generateToken(user));
            
        } catch (Exception e) {
            System.out.println("❌ Authentication failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Les identifications sont erronées");
        }
    }
}
