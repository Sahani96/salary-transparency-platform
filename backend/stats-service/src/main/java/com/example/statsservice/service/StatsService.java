package com.example.statsservice.service;

import com.example.statsservice.dto.*;
import com.example.statsservice.repository.StatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public StatsSummaryResponse getOverallSummary() {
        List<Object[]> results = statsRepository.getOverallStats();
        if (results.isEmpty() || results.get(0) == null) {
            return StatsSummaryResponse.builder()
                    .totalSubmissions(0)
                    .averageSalary(BigDecimal.ZERO)
                    .medianSalary(BigDecimal.ZERO)
                    .minSalary(BigDecimal.ZERO)
                    .maxSalary(BigDecimal.ZERO)
                    .p25Salary(BigDecimal.ZERO)
                    .p75Salary(BigDecimal.ZERO)
                    .p90Salary(BigDecimal.ZERO)
                    .build();
        }

        Object[] row = results.get(0);
        logger.info("Overall stats calculated: {} approved submissions", row[0]);

        return StatsSummaryResponse.builder()
                .totalSubmissions(((Number) row[0]).longValue())
                .averageSalary(toBigDecimal(row[1]))
                .minSalary(toBigDecimal(row[2]))
                .maxSalary(toBigDecimal(row[3]))
                .medianSalary(toBigDecimal(row[4]))
                .p25Salary(toBigDecimal(row[5]))
                .p75Salary(toBigDecimal(row[6]))
                .p90Salary(toBigDecimal(row[7]))
                .build();
    }

    public GroupedStatsResponse getStatsByRole() {
        return buildGroupedResponse("role", statsRepository.getStatsByRole());
    }

    public GroupedStatsResponse getStatsByCompany() {
        return buildGroupedResponse("company", statsRepository.getStatsByCompany());
    }

    public GroupedStatsResponse getStatsByCountry() {
        return buildGroupedResponse("country", statsRepository.getStatsByCountry());
    }

    public GroupedStatsResponse getStatsByLevel() {
        return buildGroupedResponse("experienceLevel", statsRepository.getStatsByLevel());
    }

    public ComparisonResponse compare(String compareBy, String first, String second) {
        List<Object[]> firstResults = statsRepository.getStatsForGroup(compareBy, first);
        List<Object[]> secondResults = statsRepository.getStatsForGroup(compareBy, second);

        GroupedStatsEntry firstEntry = firstResults.isEmpty() ? emptyEntry(first) : toGroupedEntry(firstResults.get(0));
        GroupedStatsEntry secondEntry = secondResults.isEmpty() ? emptyEntry(second) : toGroupedEntry(secondResults.get(0));

        return ComparisonResponse.builder()
                .compareBy(compareBy)
                .first(firstEntry)
                .second(secondEntry)
                .build();
    }

    private GroupedStatsResponse buildGroupedResponse(String groupedBy, List<Object[]> results) {
        List<GroupedStatsEntry> entries = results.stream()
                .map(this::toGroupedEntry)
                .toList();

        return GroupedStatsResponse.builder()
                .groupedBy(groupedBy)
                .entries(entries)
                .build();
    }

    private GroupedStatsEntry toGroupedEntry(Object[] row) {
        return GroupedStatsEntry.builder()
                .groupName((String) row[0])
                .count(((Number) row[1]).longValue())
                .averageSalary(toBigDecimal(row[2]))
                .minSalary(toBigDecimal(row[3]))
                .maxSalary(toBigDecimal(row[4]))
                .medianSalary(toBigDecimal(row[5]))
                .p25Salary(toBigDecimal(row[6]))
                .p75Salary(toBigDecimal(row[7]))
                .p90Salary(toBigDecimal(row[8]))
                .build();
    }

    private GroupedStatsEntry emptyEntry(String name) {
        return GroupedStatsEntry.builder()
                .groupName(name)
                .count(0)
                .averageSalary(BigDecimal.ZERO)
                .medianSalary(BigDecimal.ZERO)
                .minSalary(BigDecimal.ZERO)
                .maxSalary(BigDecimal.ZERO)
                .p25Salary(BigDecimal.ZERO)
                .p75Salary(BigDecimal.ZERO)
                .p90Salary(BigDecimal.ZERO)
                .build();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd.setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    }
}
