package com.example.voteservice.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String salaryServiceUrl;

    public SalaryStatusClient(ObjectMapper objectMapper, @Value("${salary.service.url}") String salaryServiceUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.salaryServiceUrl = salaryServiceUrl;
    }

    public void approve(UUID submissionId) {
        Map<String, String> payload = Map.of("status", "APPROVED");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(salaryServiceUrl + "/submissions/" + submissionId + "/status"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Salary service rejected status update: " + response.body());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to update salary submission status", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to update salary submission status", exception);
        }
    }
}
