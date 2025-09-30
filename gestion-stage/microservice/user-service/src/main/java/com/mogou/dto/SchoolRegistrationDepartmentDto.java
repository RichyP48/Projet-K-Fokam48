package com.mogou.dto;

import lombok.Data;

@Data
public class SchoolRegistrationDepartmentDto {
    private Long id;
    private String name;
    private String headTeacherEmail;
    private String headTeacherFirstName;
    private String headTeacherLastName;
}