package com.example.bffservice.gateway;

import com.example.bffservice.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final GatewayService gatewayService;
    private final JwtService jwtService;

    public ApiController(GatewayService gatewayService, JwtService jwtService) {
        this.gatewayService = gatewayService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, Object> body) {
        return gatewayService.post(gatewayService.identityServiceUrl() + "/auth/signup", body, null);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> body) {
        return gatewayService.post(gatewayService.identityServiceUrl() + "/auth/login", body, null);
    }

    @GetMapping("/auth/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        JwtService.TokenPrincipal principal = jwtService.validate(authorizationHeader);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", principal.valid());
        response.put("userId", principal.userId());
        response.put("username", principal.username());
        response.put("email", principal.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/salaries")
    public ResponseEntity<String> createSalary(@RequestBody Map<String, Object> body) {
        return gatewayService.post(gatewayService.salaryServiceUrl() + "/submissions", body, null);
    }

    @GetMapping("/salaries/{id}")
    public ResponseEntity<String> getSalary(@PathVariable UUID id) {
        return gatewayService.get(gatewayService.salaryServiceUrl() + "/submissions/" + id, null);
    }

    @PostMapping("/votes")
    public ResponseEntity<?> castVote(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> body
    ) {
        JwtService.TokenPrincipal principal = jwtService.validate(authorizationHeader);
        if (!principal.valid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or missing token"));
        }

        Object submissionId = body.get("submissionId");
        Object voteType = body.get("voteType");
        if (submissionId == null || voteType == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "submissionId and voteType are required"));
        }

        Map<String, Object> payload = Map.of(
                "submissionId", submissionId,
                "voterUserId", principal.userId(),
                "voteType", voteType
        );
        return gatewayService.post(gatewayService.voteServiceUrl() + "/votes", payload, authorizationHeader);
    }

    @GetMapping("/votes/submission/{submissionId}")
    public ResponseEntity<String> getVoteCounts(@PathVariable UUID submissionId) {
        return gatewayService.get(gatewayService.voteServiceUrl() + "/votes/submission/" + submissionId, null);
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(HttpServletRequest request) {
        String query = request.getQueryString();
        String url = gatewayService.searchServiceUrl() + "/search" + (query == null ? "" : "?" + query);
        return gatewayService.get(url, null);
    }

    @GetMapping("/search/filters")
    public ResponseEntity<String> searchFilters() {
        return gatewayService.get(gatewayService.searchServiceUrl() + "/search/filters", null);
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<String> statsSummary() {
        return gatewayService.get(gatewayService.statsServiceUrl() + "/stats/summary", null);
    }

    @GetMapping("/stats/by-role")
    public ResponseEntity<String> statsByRole() {
        return gatewayService.get(gatewayService.statsServiceUrl() + "/stats/by-role", null);
    }

    @GetMapping("/stats/by-company")
    public ResponseEntity<String> statsByCompany() {
        return gatewayService.get(gatewayService.statsServiceUrl() + "/stats/by-company", null);
    }

    @GetMapping("/stats/by-country")
    public ResponseEntity<String> statsByCountry() {
        return gatewayService.get(gatewayService.statsServiceUrl() + "/stats/by-country", null);
    }

    @GetMapping("/stats/by-level")
    public ResponseEntity<String> statsByLevel() {
        return gatewayService.get(gatewayService.statsServiceUrl() + "/stats/by-level", null);
    }

    @GetMapping("/stats/compare")
    public ResponseEntity<String> statsCompare(
            @RequestParam String groupBy,
            @RequestParam String values
    ) {
        String url = gatewayService.statsServiceUrl() + "/stats/compare?groupBy=" + groupBy + "&values=" + values;
        return gatewayService.get(url, null);
    }
}
