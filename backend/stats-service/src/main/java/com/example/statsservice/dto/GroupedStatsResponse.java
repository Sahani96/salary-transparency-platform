package com.example.statsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedStatsResponse {
    private String groupedBy;
    private List<GroupedStatsEntry> entries;
}
