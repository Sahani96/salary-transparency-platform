package com.example.searchservice.search;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class SearchService {

    private final SalarySubmissionRepository repository;

    public SearchService(SalarySubmissionRepository repository) {
        this.repository = repository;
    }

    public SearchDtos.SearchResponse search(
            String country,
            String company,
            String jobTitle,
            String experienceLevel,
            String employmentType,
            Double minSalary,
            Double maxSalary,
            String techStack,
            String sortBy,
            String sortDirection,
            int page,
            int size
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = mapSortField(sortBy);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sortField));

        Specification<SalarySubmission> specification = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("status"), SubmissionStatus.APPROVED));
            if (hasText(country)) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase(Locale.ROOT)));
            }
            if (hasText(company)) {
                predicates.add(cb.like(cb.lower(root.get("company")), "%" + company.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (hasText(jobTitle)) {
                predicates.add(cb.like(cb.lower(root.get("jobTitle")), "%" + jobTitle.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (hasText(experienceLevel)) {
                predicates.add(cb.equal(root.get("experienceLevel"), ExperienceLevel.valueOf(experienceLevel.trim().toUpperCase(Locale.ROOT))));
            }
            if (hasText(employmentType)) {
                predicates.add(cb.equal(root.get("employmentType"), EmploymentType.valueOf(employmentType.trim().toUpperCase(Locale.ROOT))));
            }
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("baseSalary"), java.math.BigDecimal.valueOf(minSalary)));
            }
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("baseSalary"), java.math.BigDecimal.valueOf(maxSalary)));
            }
            if (hasText(techStack)) {
                predicates.add(cb.like(cb.lower(root.get("techStack")), "%" + techStack.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<SalarySubmission> results = repository.findAll(specification, pageable);
        List<SearchDtos.SearchResult> content = results.getContent().stream()
                .map(submission -> new SearchDtos.SearchResult(
                        submission.getId(),
                        submission.getJobTitle(),
                        submission.getCompany(),
                        submission.getCountry(),
                        submission.getCity(),
                        submission.getExperienceLevel(),
                        submission.getYearsOfExperience(),
                        submission.getBaseSalary(),
                        submission.getCurrency(),
                        submission.getEmploymentType(),
                        submission.getTechStack(),
                        submission.getSubmittedAt()
                ))
                .toList();

        return new SearchDtos.SearchResponse(content, results.getNumber(), results.getSize(), results.getTotalElements(), results.getTotalPages());
    }

    public SearchDtos.FilterOptionsResponse filters() {
        return new SearchDtos.FilterOptionsResponse(
                repository.findCountries(),
                repository.findCompanies(),
                repository.findJobTitles(),
                Arrays.stream(ExperienceLevel.values()).map(Enum::name).toList(),
                Arrays.stream(EmploymentType.values()).map(Enum::name).toList()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "submittedAt";
        }
        return switch (sortBy) {
            case "jobTitle" -> "jobTitle";
            case "company" -> "company";
            case "country" -> "country";
            case "baseSalary" -> "baseSalary";
            case "experienceLevel" -> "experienceLevel";
            default -> "submittedAt";
        };
    }
}
