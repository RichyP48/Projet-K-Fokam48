package com.mogou.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conventions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long candidatureId;

    @Column(nullable = false)
    private Long enseignantId;

    @Column(nullable = false)
    private Long entrepriseId;

    @Column(nullable = false)
    private Long etudiantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConvention statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateValidation;

    private String documentPath; // Chemin vers le PDF dans MinIO
}
