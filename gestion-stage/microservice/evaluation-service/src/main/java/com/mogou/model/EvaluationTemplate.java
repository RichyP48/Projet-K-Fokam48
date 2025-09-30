package com.mogou.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class EvaluationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filiere; // Ex: "Informatique", "Mécanique"

    @Enumerated(EnumType.STRING)
    private TypeEvaluation typeEvaluation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private List<CritereEvaluation> criteres;
}