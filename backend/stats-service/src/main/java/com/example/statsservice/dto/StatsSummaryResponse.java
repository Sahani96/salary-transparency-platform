package com.example.statsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSummaryResponse {
    private long totalSubmissions;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal p25Salary;
    private BigDecimal p75Salary;
    private BigDecimal p90Salary;
}
