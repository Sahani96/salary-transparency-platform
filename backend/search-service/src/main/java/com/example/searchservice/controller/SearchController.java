package com.example.searchservice.controller;

import com.example.searchservice.dto.FilterOptionsResponse;
import com.example.searchservice.dto.SearchResponse;
import com.example.searchservice.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String techStack,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        SearchResponse response = searchService.search(
                country, company, role, level, minSalary, maxSalary,
                employmentType, techStack, page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filters")
    public ResponseEntity<FilterOptionsResponse> getFilterOptions() {
        return ResponseEntity.ok(searchService.getFilterOptions());
    }
}
