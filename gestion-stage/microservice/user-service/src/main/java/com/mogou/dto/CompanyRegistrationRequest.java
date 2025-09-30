package com.mogou.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {
    @NotBlank @Email
    private String contactEmail;
    
    @NotBlank @Size(min = 8)
    private String password;
    
    @NotBlank
    private String companyName;
    
    private String companyIndustrySector;
}