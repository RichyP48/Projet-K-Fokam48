package com.mogou.model;


import com.mogou.enums.StatutCandidature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long etudiantId;

    @Column(nullable = false)
    private Long offreId;

    @Column(nullable = false)
    private Long entrepriseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut;

    @Column(nullable = false)
    private LocalDateTime datePostulation;

    private String cvPath;
    private String lettreMotivationPath;

    @Column(columnDefinition = "TEXT")
    private String commentaires;
}
