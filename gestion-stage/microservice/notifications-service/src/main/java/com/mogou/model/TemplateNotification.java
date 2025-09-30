package com.mogou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TemplateNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String type; // Ex: "NOUVELLE_CANDIDATURE", "CONVENTION_SIGNEE"

    @Column(nullable = false)
    private String sujet;

    @Column(columnDefinition = "TEXT")
    private String contenuHtml;
}
