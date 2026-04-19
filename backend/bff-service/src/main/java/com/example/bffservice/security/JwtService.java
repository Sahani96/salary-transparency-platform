package com.example.bffservice.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public record TokenPrincipal(boolean valid, UUID userId, String username, String email) {
    }

    private final ObjectMapper objectMapper;
    private final byte[] secret;

    public JwtService(ObjectMapper objectMapper, @Value("${jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = Base64.getDecoder().decode(secret);
    }

    public TokenPrincipal validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return new TokenPrincipal(false, null, null, null);
        }

        String token = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return new TokenPrincipal(false, null, null, null);
        }

        try {
            String content = parts[0] + "." + parts[1];
            if (!hmac(content).equals(parts[2])) {
                return new TokenPrincipal(false, null, null, null);
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return new TokenPrincipal(false, null, null, null);
            }

            return new TokenPrincipal(
                    true,
                    UUID.fromString(payload.get("userId").toString()),
                    payload.get("sub").toString(),
                    payload.get("email").toString()
            );
        } catch (Exception exception) {
            logger.debug("Token validation failed: {}", exception.getMessage());
            return new TokenPrincipal(false, null, null, null);
        }
    }

    private String hmac(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
