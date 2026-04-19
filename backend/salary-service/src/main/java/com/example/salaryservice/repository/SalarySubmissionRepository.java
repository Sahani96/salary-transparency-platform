package com.example.salaryservice.repository;

import com.example.salaryservice.entity.SalarySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SalarySubmissionRepository extends JpaRepository<SalarySubmission, UUID> {
}
