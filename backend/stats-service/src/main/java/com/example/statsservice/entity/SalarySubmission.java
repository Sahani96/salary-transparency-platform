package com.example.statsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions", schema = "salary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalarySubmission {

    @Id
    private UUID id;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "experience_level", nullable = false, length = 20)
    private String experienceLevel;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "employment_type", nullable = false, length = 20)
    private String employmentType;

    @Column(nullable = false)
    private Boolean anonymize;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "tech_stack", length = 500)
    private String techStack;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}
