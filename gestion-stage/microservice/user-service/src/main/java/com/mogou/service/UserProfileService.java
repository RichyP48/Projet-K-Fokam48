package com.mogou.service;



import com.mogou.dto.UserProfileDto;
import com.mogou.model.Role;
import com.mogou.model.User;
import com.mogou.model.UserProfile;
import com.mogou.repository.UserProfileRepository;
import com.mogou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        UserProfile profile = user.getUserProfile();
        return convertToDto(profile);
    }

    @Transactional
    public UserProfileDto updateUserProfileByEmail(String email, UserProfileDto profileDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        UserProfile profile = user.getUserProfile();

        // Mettre à jour les champs
        profile.setNom(profileDto.getNom());
        profile.setPrenom(profileDto.getPrenom());
        profile.setTelephone(profileDto.getTelephone());
        profile.setAdresse(profileDto.getAdresse());
        profile.setSpecificAttributes(profileDto.getSpecificAttributes());

        userProfileRepository.save(profile);
        return convertToDto(profile);
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentsByTeacherDepartment(String teacherEmail) {
        try {
            // Récupérer l'enseignant et son département
            User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'email: " + teacherEmail));
            
            if (teacher.getUserProfile() == null) {
                throw new RuntimeException("Profil enseignant non configuré. Veuillez compléter votre profil.");
            }
            
            if (teacher.getUserProfile().getDepartment() == null) {
                throw new RuntimeException("Département non assigné. Contactez l'administration pour assigner un département.");
            }
            
            Long departmentId = teacher.getUserProfile().getDepartment().getId();
            log.info("🔍 Recherche étudiants pour le département ID: {}", departmentId);
            
            // Récupérer les étudiants du même département
            List<User> students = userRepository.findByRole(Role.ETUDIANT).stream()
                .filter(user -> user.getUserProfile() != null && 
                               user.getUserProfile().getDepartment() != null &&
                               departmentId.equals(user.getUserProfile().getDepartment().getId()))
                .collect(Collectors.toList());
            log.info("📊 Nombre d'étudiants trouvés: {}", students.size());
            
            return students.stream()
                .map(this::convertToUserMap)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des étudiants pour l'enseignant {}: {}", teacherEmail, e.getMessage(), e);
            throw e; // Propager l'exception pour que le frontend reçoive le message
        }
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCompaniesForTeacherStudents(String teacherEmail) {
        try {
            log.info("🔍 Récupération des entreprises pour l'enseignant: {}", teacherEmail);
            
            // Récupérer toutes les entreprises actives
            List<User> companies = userRepository.findByRole(Role.ENTREPRISE).stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
            log.info("🏢 Nombre d'entreprises actives trouvées: {}", companies.size());
            
            if (companies.isEmpty()) {
                throw new RuntimeException("Aucune entreprise active n'est enregistrée dans le système.");
            }
            
            return companies.stream()
                .map(this::convertToUserMap)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des entreprises pour l'enseignant {}: {}", teacherEmail, e.getMessage(), e);
            throw e; // Propager l'exception pour que le frontend reçoive le message
        }
    }

    private UserProfileDto convertToDto(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        UserProfileDto dto = new UserProfileDto();
        dto.setNom(profile.getNom());
        dto.setPrenom(profile.getPrenom());
        dto.setTelephone(profile.getTelephone());
        dto.setAdresse(profile.getAdresse());
        dto.setSpecificAttributes(profile.getSpecificAttributes());
        return dto;
    }

    private Map<String, Object> convertToUserMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("active", user.isActive());
        userMap.put("role", user.getRole().name());

        if (user.getUserProfile() != null) {
            userMap.put("nom", user.getUserProfile().getNom());
            userMap.put("prenom", user.getUserProfile().getPrenom());
            userMap.put("telephone", user.getUserProfile().getTelephone());
            if (user.getUserProfile().getDepartment() != null) {
                userMap.put("department", user.getUserProfile().getDepartment().getName());
            }
        }
        return userMap;
    }
}
