package com.example.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOptionsResponse {
    private List<String> countries;
    private List<String> companies;
    private List<String> jobTitles;
    private List<String> experienceLevels;
    private List<String> employmentTypes;
}
