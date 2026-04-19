package com.example.salaryservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalarySubmissionRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Country is required")
    private String country;

    private String city;

    @NotBlank(message = "Experience level is required")
    @Pattern(regexp = "JUNIOR|MID|SENIOR|LEAD|PRINCIPAL", message = "Invalid experience level")
    private String experienceLevel;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience must be non-negative")
    private Integer yearsOfExperience;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Salary must be non-negative")
    private BigDecimal baseSalary;

    private String currency = "LKR";

    @Pattern(regexp = "FULL_TIME|PART_TIME|CONTRACT|FREELANCE", message = "Invalid employment type")
    private String employmentType = "FULL_TIME";

    private Boolean anonymize = false;

    private String techStack;
}
