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
        // Cette méthode ne gère que les étudiants
        if (request.getRole() == Role.ENSEIGNANT) {
            throw new RuntimeException("Utilisez l'endpoint spécifique pour l'enregistrement des écoles");
        }
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
        
        // Assignation des références académiques pour les étudiants
        if (request.getRole() == Role.ETUDIANT) {
            if (request.getProfile().getSchoolId() != null) {
                profile.setSchool(schoolRepository.findById(request.getProfile().getSchoolId()).orElse(null));
            }
            if (request.getProfile().getFacultyId() != null) {
                profile.setFaculty(facultyRepository.findById(request.getProfile().getFacultyId()).orElse(null));
            }
            if (request.getProfile().getDepartmentId() != null) {
                profile.setDepartment(departmentRepository.findById(request.getProfile().getDepartmentId()).orElse(null));
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return new AuthResponse(jwtUtil.generateToken(user));
    }
}
