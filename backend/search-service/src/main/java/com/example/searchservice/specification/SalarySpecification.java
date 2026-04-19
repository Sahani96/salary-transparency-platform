package com.example.searchservice.specification;

import com.example.searchservice.entity.SalarySubmission;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SalarySpecification {

    private SalarySpecification() {
    }

    public static Specification<SalarySubmission> isApproved() {
        return (root, query, cb) -> cb.equal(root.get("status"), "APPROVED");
    }

    public static Specification<SalarySubmission> hasCountry(String country) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("country")), country.toLowerCase());
    }

    public static Specification<SalarySubmission> hasCompanyLike(String company) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("company")), "%" + company.toLowerCase() + "%");
    }

    public static Specification<SalarySubmission> hasJobTitleLike(String jobTitle) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("jobTitle")), "%" + jobTitle.toLowerCase() + "%");
    }

    public static Specification<SalarySubmission> hasExperienceLevel(String level) {
        return (root, query, cb) ->
                cb.equal(root.get("experienceLevel"), level);
    }

    public static Specification<SalarySubmission> hasEmploymentType(String type) {
        return (root, query, cb) ->
                cb.equal(root.get("employmentType"), type);
    }

    public static Specification<SalarySubmission> hasSalaryGreaterThanOrEqual(BigDecimal minSalary) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("baseSalary"), minSalary);
    }

    public static Specification<SalarySubmission> hasSalaryLessThanOrEqual(BigDecimal maxSalary) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("baseSalary"), maxSalary);
    }

    public static Specification<SalarySubmission> hasTechStackLike(String techStack) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("techStack")), "%" + techStack.toLowerCase() + "%");
    }
}
