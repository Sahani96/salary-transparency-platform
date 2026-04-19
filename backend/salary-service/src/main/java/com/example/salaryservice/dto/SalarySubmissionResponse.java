package com.example.salaryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySubmissionResponse {
    private String id;
    private String jobTitle;
    private String company;
    private String country;
    private String city;
    private String experienceLevel;
    private Integer yearsOfExperience;
    private BigDecimal baseSalary;
    private String currency;
    private String employmentType;
    private Boolean anonymize;
    private String status;
    private String techStack;
    private LocalDateTime submittedAt;
}
