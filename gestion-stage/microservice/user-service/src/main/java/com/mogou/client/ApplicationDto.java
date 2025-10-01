package com.mogou.client;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationDto {
    private Long id;
    private Long etudiantId;
    private Long offreId;
    private String statut;
    private LocalDateTime datePostulation;
    private String offerTitle;
    private String studentName;
    private String companyName;
    private String location;
    private String duration;
}