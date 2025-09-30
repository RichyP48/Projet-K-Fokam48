package com.mogou.model;


import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    
    // Références académiques pour étudiants
    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;
    
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    // Niveau d'études pour étudiants (L1, L2, L3, M1, M2, etc.)
    private String studyLevel;
    
    // Colonne JSONB pour les attributs spécifiques à chaque rôle
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> specificAttributes;
}
