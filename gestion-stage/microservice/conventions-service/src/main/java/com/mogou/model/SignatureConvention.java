package com.mogou.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "signatures_convention")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureConvention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "convention_id", nullable = false)
    private Convention convention;

    @Column(nullable = false)
    private Long signataireId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSignataire typeSignataire;

    @Column(nullable = false)
    private LocalDateTime dateSignature;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false, length = 64) // SHA-256 hash
    private String documentHash;
}
