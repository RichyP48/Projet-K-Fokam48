package com.mogou.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConventionResponseDto {
    private Long id;
    private String offerTitle;
    private String studentName;
    private String status; // English status
    private boolean signedByStudent;
    private boolean signedByCompany;
    private boolean signedByFaculty;
    private boolean approvedByAdmin;
    private LocalDateTime studentSignatureDate;
    private LocalDateTime companySignatureDate;
    private LocalDateTime facultyValidationDate;
    private LocalDateTime adminApprovalDate;
}