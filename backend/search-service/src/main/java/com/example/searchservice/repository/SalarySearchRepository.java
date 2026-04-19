package com.example.searchservice.repository;

import com.example.searchservice.entity.SalarySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalarySearchRepository extends JpaRepository<SalarySubmission, UUID>,
        JpaSpecificationExecutor<SalarySubmission> {

    @Query("SELECT DISTINCT s.country FROM SalarySubmission s WHERE s.status = 'APPROVED' ORDER BY s.country")
    List<String> findDistinctCountries();

    @Query("SELECT DISTINCT s.company FROM SalarySubmission s WHERE s.status = 'APPROVED' AND s.anonymize = false ORDER BY s.company")
    List<String> findDistinctCompanies();

    @Query("SELECT DISTINCT s.jobTitle FROM SalarySubmission s WHERE s.status = 'APPROVED' ORDER BY s.jobTitle")
    List<String> findDistinctJobTitles();

    @Query("SELECT DISTINCT s.experienceLevel FROM SalarySubmission s WHERE s.status = 'APPROVED' ORDER BY s.experienceLevel")
    List<String> findDistinctExperienceLevels();

    @Query("SELECT DISTINCT s.employmentType FROM SalarySubmission s WHERE s.status = 'APPROVED' ORDER BY s.employmentType")
    List<String> findDistinctEmploymentTypes();
}
