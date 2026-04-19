package com.example.statsservice.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalarySubmissionRepository extends JpaRepository<SalarySubmission, UUID> {

    List<SalarySubmission> findByStatus(SubmissionStatus status);
}
