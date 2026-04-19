package com.example.statsservice.controller;

import com.example.statsservice.dto.ComparisonResponse;
import com.example.statsservice.dto.GroupedStatsResponse;
import com.example.statsservice.dto.StatsSummaryResponse;
import com.example.statsservice.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary() {
        return ResponseEntity.ok(statsService.getOverallSummary());
    }

    @GetMapping("/by-role")
    public ResponseEntity<GroupedStatsResponse> getByRole() {
        return ResponseEntity.ok(statsService.getStatsByRole());
    }

    @GetMapping("/by-company")
    public ResponseEntity<GroupedStatsResponse> getByCompany() {
        return ResponseEntity.ok(statsService.getStatsByCompany());
    }

    @GetMapping("/by-country")
    public ResponseEntity<GroupedStatsResponse> getByCountry() {
        return ResponseEntity.ok(statsService.getStatsByCountry());
    }

    @GetMapping("/by-level")
    public ResponseEntity<GroupedStatsResponse> getByLevel() {
        return ResponseEntity.ok(statsService.getStatsByLevel());
    }

    @GetMapping("/compare")
    public ResponseEntity<ComparisonResponse> compare(
            @RequestParam String compareBy,
            @RequestParam String first,
            @RequestParam String second) {
        return ResponseEntity.ok(statsService.compare(compareBy, first, second));
    }
}
