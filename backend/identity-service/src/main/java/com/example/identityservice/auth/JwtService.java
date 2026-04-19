package com.example.identityservice.auth;

import com.example.identityservice.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationMillis;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis
    ) {
        this.objectMapper = objectMapper;
        this.secret = Base64.getDecoder().decode(secret);
        this.expirationMillis = expirationMillis;
    }

    public AuthDtos.AuthResponse createToken(User user) {
        Instant expiration = Instant.now().plusMillis(expirationMillis);
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("userId", user.getId().toString());
        payload.put("email", user.getEmail());
        payload.put("exp", expiration.getEpochSecond());

        String token = sign(header, payload);
        return new AuthDtos.AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                LocalDateTime.ofInstant(expiration, ZoneOffset.UTC)
        );
    }

    public AuthDtos.TokenValidationResponse validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return new AuthDtos.TokenValidationResponse(false, null, null, null);
        }

        String token = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return new AuthDtos.TokenValidationResponse(false, null, null, null);
        }

        try {
            String signature = hmac(parts[0] + "." + parts[1]);
            if (!signature.equals(parts[2])) {
                return new AuthDtos.TokenValidationResponse(false, null, null, null);
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return new AuthDtos.TokenValidationResponse(false, null, null, null);
            }

            return new AuthDtos.TokenValidationResponse(
                    true,
                    UUID.fromString(payload.get("userId").toString()),
                    payload.get("sub").toString(),
                    payload.get("email").toString()
            );
        } catch (Exception exception) {
            return new AuthDtos.TokenValidationResponse(false, null, null, null);
        }
    }

    private String sign(Map<String, Object> header, Map<String, Object> payload) {
        try {
            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));
            String content = encodedHeader + "." + encodedPayload;
            return content + "." + hmac(content);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create token", exception);
        }
    }

    private String hmac(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
