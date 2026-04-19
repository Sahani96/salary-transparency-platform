package com.example.statsservice.stats;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

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

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
