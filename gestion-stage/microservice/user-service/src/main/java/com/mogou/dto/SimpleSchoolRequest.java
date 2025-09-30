package com.mogou.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SimpleSchoolRequest {
    @NotBlank @Email
    private String contactEmail;
    
    @NotBlank @Size(min = 8)
    private String password;
    
    @NotBlank
    private String schoolName;
    
    private String schoolAddress;
    private String schoolDescription;
}