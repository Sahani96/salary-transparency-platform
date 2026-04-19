package com.example.searchservice.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SalarySubmissionRepository extends JpaRepository<SalarySubmission, UUID>, JpaSpecificationExecutor<SalarySubmission> {

    @Query("select distinct s.company from SalarySubmission s where s.status = 'APPROVED' order by s.company")
    List<String> findCompanies();

    @Query("select distinct s.country from SalarySubmission s where s.status = 'APPROVED' order by s.country")
    List<String> findCountries();

    @Query("select distinct s.jobTitle from SalarySubmission s where s.status = 'APPROVED' order by s.jobTitle")
    List<String> findJobTitles();
}
