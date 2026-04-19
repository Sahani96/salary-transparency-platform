package com.example.salaryservice.submission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalarySubmissionRepository extends JpaRepository<SalarySubmission, UUID> {
}
