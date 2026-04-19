package com.example.searchservice.dto;

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
public class SalarySearchResult {
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
    private String techStack;
    private LocalDateTime submittedAt;
}
