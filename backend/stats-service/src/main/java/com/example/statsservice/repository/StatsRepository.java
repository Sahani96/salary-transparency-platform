package com.example.statsservice.repository;

import com.example.statsservice.entity.SalarySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatsRepository extends JpaRepository<SalarySubmission, UUID> {

    // Overall summary for approved records
    @Query(value = """
            SELECT COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED'
            """, nativeQuery = true)
    List<Object[]> getOverallStats();

    // Stats grouped by job title
    @Query(value = """
            SELECT job_title as group_name,
                   COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED'
            GROUP BY job_title
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> getStatsByRole();

    // Stats grouped by company (excluding anonymized)
    @Query(value = """
            SELECT company as group_name,
                   COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED' AND anonymize = false
            GROUP BY company
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> getStatsByCompany();

    // Stats grouped by country
    @Query(value = """
            SELECT country as group_name,
                   COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED'
            GROUP BY country
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> getStatsByCountry();

    // Stats grouped by experience level
    @Query(value = """
            SELECT experience_level as group_name,
                   COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED'
            GROUP BY experience_level
            ORDER BY avg_salary DESC
            """, nativeQuery = true)
    List<Object[]> getStatsByLevel();

    // Stats for a specific group (for comparison)
    @Query(value = """
            SELECT :groupValue as group_name,
                   COUNT(*) as count,
                   COALESCE(AVG(base_salary), 0) as avg_salary,
                   COALESCE(MIN(base_salary), 0) as min_salary,
                   COALESCE(MAX(base_salary), 0) as max_salary,
                   COALESCE(PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY base_salary), 0) as median_salary,
                   COALESCE(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY base_salary), 0) as p25_salary,
                   COALESCE(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY base_salary), 0) as p75_salary,
                   COALESCE(PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY base_salary), 0) as p90_salary
            FROM salary.submissions
            WHERE status = 'APPROVED'
              AND ((:groupBy = 'role' AND LOWER(job_title) = LOWER(:groupValue))
                OR (:groupBy = 'company' AND LOWER(company) = LOWER(:groupValue))
                OR (:groupBy = 'country' AND LOWER(country) = LOWER(:groupValue))
                OR (:groupBy = 'level' AND experience_level = :groupValue))
            """, nativeQuery = true)
    List<Object[]> getStatsForGroup(@Param("groupBy") String groupBy, @Param("groupValue") String groupValue);
}
