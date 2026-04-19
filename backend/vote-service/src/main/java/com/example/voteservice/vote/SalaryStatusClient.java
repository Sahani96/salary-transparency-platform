package com.example.voteservice.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@Component
public class SalaryStatusClient {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStatusClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String salaryServiceUrl;

    public SalaryStatusClient(ObjectMapper objectMapper, @Value("${salary.service.url}") String salaryServiceUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.salaryServiceUrl = salaryServiceUrl;
    }

    public void approve(UUID submissionId) {
        patch("/api/salaries/" + submissionId + "/status", Map.of("status", "APPROVED"));
    }

    private void patch(String path, Object payload) {
        String url = salaryServiceUrl + path;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.error("PATCH {} -> salary-service returned {}: {}", url, response.statusCode(), response.body());
                throw new IllegalStateException("Salary service rejected request: " + response.body());
            }
            logger.debug("PATCH {} -> {}", url, response.statusCode());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.error("PATCH {} -> interrupted", url, exception);
            throw new IllegalStateException("Failed to call salary service", exception);
        } catch (IOException exception) {
            logger.error("PATCH {} -> I/O error: {}", url, exception.getMessage(), exception);
            throw new IllegalStateException("Failed to call salary service", exception);
        }
    }
}
