package com.mogou.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class SchoolRegistrationRequest {
    // Informations de contact
    @NotBlank @Email
    private String contactEmail;
    
    @NotBlank @Size(min = 8)
    private String password;
    
    // Informations école
    @NotBlank
    private String schoolName;
    
    private String schoolAddress;
    private String schoolDescription;
    
    // Structure académique
    @NotEmpty @Valid
    private List<SchoolRegistrationFacultyDto> faculties;
}

