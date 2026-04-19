package com.example.statsservice.stats;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<StatsDtos.SummaryResponse> summary() {
        return ResponseEntity.ok(statsService.summary());
    }

    @GetMapping("/by-role")
    public ResponseEntity<StatsDtos.GroupStatsResponse> byRole() {
        return ResponseEntity.ok(statsService.byRole());
    }

    @GetMapping("/by-company")
    public ResponseEntity<StatsDtos.GroupStatsResponse> byCompany() {
        return ResponseEntity.ok(statsService.byCompany());
    }

    @GetMapping("/by-country")
    public ResponseEntity<StatsDtos.GroupStatsResponse> byCountry() {
        return ResponseEntity.ok(statsService.byCountry());
    }

    @GetMapping("/by-level")
    public ResponseEntity<StatsDtos.GroupStatsResponse> byLevel() {
        return ResponseEntity.ok(statsService.byLevel());
    }

    @GetMapping("/compare")
    public ResponseEntity<StatsDtos.GroupStatsResponse> compare(
            @RequestParam String groupBy,
            @RequestParam String values
    ) {
        List<String> requestedValues = Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        return ResponseEntity.ok(statsService.compare(groupBy, requestedValues));
    }
}
