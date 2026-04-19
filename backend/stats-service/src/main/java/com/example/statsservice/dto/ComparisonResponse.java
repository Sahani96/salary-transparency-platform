package com.example.statsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonResponse {
    private String compareBy;
    private GroupedStatsEntry first;
    private GroupedStatsEntry second;
}
