package com.example.searchservice.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions", schema = "salary")
public class SalarySubmission {

    @Id
    private UUID id;

    @Column(name = "job_title")
    private String jobTitle;

    private String company;

    private String country;

    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    private boolean anonymize;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "tech_stack")
    private String techStack;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    public UUID getId() {
        return id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public String getCurrency() {
        return currency;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public boolean isAnonymize() {
        return anonymize;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public String getTechStack() {
        return techStack;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
