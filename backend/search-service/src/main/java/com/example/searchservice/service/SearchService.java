package com.example.searchservice.service;

import com.example.searchservice.dto.FilterOptionsResponse;
import com.example.searchservice.dto.SalarySearchResult;
import com.example.searchservice.dto.SearchResponse;
import com.example.searchservice.entity.SalarySubmission;
import com.example.searchservice.repository.SalarySearchRepository;
import com.example.searchservice.specification.SalarySpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final SalarySearchRepository repository;

    public SearchService(SalarySearchRepository repository) {
        this.repository = repository;
    }

    public SearchResponse search(String country, String company, String role,
                                  String level, BigDecimal minSalary, BigDecimal maxSalary,
                                  String employmentType, String techStack,
                                  int page, int size, String sortBy, String sortDir) {

        // Build dynamic specification — always filter by APPROVED status
        Specification<SalarySubmission> spec = Specification.where(SalarySpecification.isApproved()).or(SalarySpecification.isPending());

        if (country != null && !country.isBlank()) {
            spec = spec.and(SalarySpecification.hasCountry(country));
        }
        if (company != null && !company.isBlank()) {
            spec = spec.and(SalarySpecification.hasCompanyLike(company));
        }
        if (role != null && !role.isBlank()) {
            spec = spec.and(SalarySpecification.hasJobTitleLike(role));
        }
        if (level != null && !level.isBlank()) {
            spec = spec.and(SalarySpecification.hasExperienceLevel(level));
        }
        if (minSalary != null) {
            spec = spec.and(SalarySpecification.hasSalaryGreaterThanOrEqual(minSalary));
        }
        if (maxSalary != null) {
            spec = spec.and(SalarySpecification.hasSalaryLessThanOrEqual(maxSalary));
        }
        if (employmentType != null && !employmentType.isBlank()) {
            spec = spec.and(SalarySpecification.hasEmploymentType(employmentType));
        }
        if (techStack != null && !techStack.isBlank()) {
            spec = spec.and(SalarySpecification.hasTechStackLike(techStack));
        }

        // Build pageable with sorting
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mapSortField(sortBy)
        );
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<SalarySubmission> resultPage = repository.findAll(spec, pageable);
        logger.info("Search returned {} results (page {} of {})", resultPage.getTotalElements(), page, resultPage.getTotalPages());

        List<SalarySearchResult> results = resultPage.getContent().stream()
                .map(this::toSearchResult)
                .toList();

        return SearchResponse.builder()
                .results(results)
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .build();
    }

    public FilterOptionsResponse getFilterOptions() {
        return FilterOptionsResponse.builder()
                .countries(repository.findDistinctCountries())
                .companies(repository.findDistinctCompanies())
                .jobTitles(repository.findDistinctJobTitles())
                .experienceLevels(repository.findDistinctExperienceLevels())
                .employmentTypes(repository.findDistinctEmploymentTypes())
                .build();
    }

    private SalarySearchResult toSearchResult(SalarySubmission entity) {
        return SalarySearchResult.builder()
                .id(entity.getId().toString())
                .jobTitle(entity.getJobTitle())
                .company(entity.getAnonymize() ? "Anonymous" : entity.getCompany())
                .country(entity.getCountry())
                .city(entity.getAnonymize() ? entity.getCountry() : entity.getCity())
                .experienceLevel(entity.getExperienceLevel())
                .yearsOfExperience(entity.getYearsOfExperience())
                .baseSalary(entity.getBaseSalary())
                .currency(entity.getCurrency())
                .employmentType(entity.getEmploymentType())
                .techStack(entity.getTechStack())
                .submittedAt(entity.getSubmittedAt())
                .build();
    }

    private String mapSortField(String field) {
        if (field == null) return "submittedAt";
        return switch (field.toLowerCase()) {
            case "salary", "basesalary" -> "baseSalary";
            case "company" -> "company";
            case "role", "jobtitle" -> "jobTitle";
            case "level", "experiencelevel" -> "experienceLevel";
            case "country" -> "country";
            case "date", "submittedat" -> "submittedAt";
            default -> "submittedAt";
        };
    }
}
