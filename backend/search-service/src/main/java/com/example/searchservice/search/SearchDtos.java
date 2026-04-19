package com.example.searchservice.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class SearchDtos {

    private SearchDtos() {
    }

    public record SearchResult(
            UUID id,
            String jobTitle,
            String company,
            String country,
            String city,
            ExperienceLevel experienceLevel,
            Integer yearsOfExperience,
            BigDecimal baseSalary,
            String currency,
            EmploymentType employmentType,
            String techStack,
            LocalDateTime submittedAt
    ) {
    }

    public record SearchResponse(
            List<SearchResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record FilterOptionsResponse(
            List<String> countries,
            List<String> companies,
            List<String> jobTitles,
            List<String> experienceLevels,
            List<String> employmentTypes
    ) {
    }
}
