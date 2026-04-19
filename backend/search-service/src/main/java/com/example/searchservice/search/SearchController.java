package com.example.searchservice.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchDtos.SearchResponse> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false, defaultValue = "submittedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(searchService.search(
                country,
                company,
                jobTitle,
                experienceLevel,
                employmentType,
                minSalary,
                maxSalary,
                techStack,
                sortBy,
                sortDirection,
                page,
                size
        ));
    }

    @GetMapping("/filters")
    public ResponseEntity<SearchDtos.FilterOptionsResponse> filters() {
        return ResponseEntity.ok(searchService.filters());
    }
}
