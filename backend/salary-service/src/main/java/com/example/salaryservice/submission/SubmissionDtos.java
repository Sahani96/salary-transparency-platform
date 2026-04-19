package com.example.salaryservice.submission;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class SubmissionDtos {

    private SubmissionDtos() {
    }

    public record CreateSubmissionRequest(
            @NotBlank String jobTitle,
            @NotBlank String company,
            @NotBlank String country,
            String city,
            @NotNull ExperienceLevel experienceLevel,
            @NotNull @Min(0) Integer yearsOfExperience,
            @NotNull @DecimalMin("0.0") BigDecimal baseSalary,
            @NotBlank String currency,
            @NotNull EmploymentType employmentType,
            boolean anonymize,
            String techStack
    ) {
    }

    public record UpdateStatusRequest(
            @NotNull SubmissionStatus status
    ) {
    }

    public record SubmissionResponse(
            UUID id,
            String jobTitle,
            String company,
            String country,
            String city,
            ExperienceLevel experienceLevel,
            Integer yearsOfExperience,
            BigDecimal baseSalary,
            String currency,
            EmploymentType employmentType,
            boolean anonymize,
            SubmissionStatus status,
            String techStack,
            LocalDateTime submittedAt
    ) {
    }
}
