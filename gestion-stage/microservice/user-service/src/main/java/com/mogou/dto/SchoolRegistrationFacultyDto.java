package com.mogou.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchoolRegistrationFacultyDto {
    private Long id;
    private String name;
    private List<SchoolRegistrationDepartmentDto> departments;
}