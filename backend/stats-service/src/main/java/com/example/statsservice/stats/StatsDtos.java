package com.example.statsservice.stats;

import java.math.BigDecimal;
import java.util.List;

public final class StatsDtos {

    private StatsDtos() {
    }

    public record SummaryResponse(
            long totalSubmissions,
            BigDecimal averageSalary,
            BigDecimal medianSalary,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            BigDecimal percentile90Salary
    ) {
    }

    public record GroupStat(
            String group,
            long count,
            BigDecimal averageSalary,
            BigDecimal medianSalary,
            BigDecimal minSalary,
            BigDecimal maxSalary
    ) {
    }

    public record GroupStatsResponse(
            String groupBy,
            List<GroupStat> items
    ) {
    }
}
