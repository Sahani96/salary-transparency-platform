package com.example.bffservice.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class GatewayService {

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String identityServiceUrl;
    private final String salaryServiceUrl;
    private final String voteServiceUrl;
    private final String searchServiceUrl;
    private final String statsServiceUrl;

    public GatewayService(
            ObjectMapper objectMapper,
            @Value("${identity.service.url}") String identityServiceUrl,
            @Value("${salary.service.url}") String salaryServiceUrl,
            @Value("${vote.service.url}") String voteServiceUrl,
            @Value("${search.service.url}") String searchServiceUrl,
            @Value("${stats.service.url}") String statsServiceUrl
    ) {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.identityServiceUrl = identityServiceUrl;
        this.salaryServiceUrl = salaryServiceUrl;
        this.voteServiceUrl = voteServiceUrl;
        this.searchServiceUrl = searchServiceUrl;
        this.statsServiceUrl = statsServiceUrl;
    }

    public String identityServiceUrl() {
        return identityServiceUrl;
    }

    public String salaryServiceUrl() {
        return salaryServiceUrl;
    }

    public String voteServiceUrl() {
        return voteServiceUrl;
    }

    public String searchServiceUrl() {
        return searchServiceUrl;
    }

    public String statsServiceUrl() {
        return statsServiceUrl;
    }

    public ResponseEntity<String> get(String url, String authorizationHeader) {
        return send("GET", url, null, authorizationHeader);
    }

    public ResponseEntity<String> post(String url, Object body, String authorizationHeader) {
        return send("POST", url, body, authorizationHeader);
    }

    public ResponseEntity<String> patch(String url, Object body, String authorizationHeader) {
        return send("PATCH", url, body, authorizationHeader);
    }

    private ResponseEntity<String> send(String method, String url, Object body, String authorizationHeader) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE);

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                builder.header("Authorization", authorizationHeader);
            }

            if (body != null) {
                builder.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            }

            HttpRequest request = switch (method) {
                case "POST" -> builder.POST(body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))).build();
                case "PATCH" -> builder.method("PATCH", body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))).build();
                default -> builder.GET().build();
            };

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(HttpStatus.valueOf(response.statusCode()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.body());
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"Gateway I/O error\"}");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"Gateway request interrupted\"}");
        }
    }
}
