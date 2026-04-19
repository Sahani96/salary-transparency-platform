package com.example.identityservice.auth;

import com.example.identityservice.user.User;
import com.example.identityservice.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            logger.warn("Signup failed: username '{}' already exists", request.username());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            logger.warn("Signup failed: email already registered");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        AuthDtos.AuthResponse response = jwtService.createToken(userRepository.save(user));
        logger.info("User '{}' signed up successfully", user.getUsername());
        return response;
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> {
                    logger.warn("Login failed: username '{}' not found", request.username());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logger.warn("Login failed: incorrect password for user '{}'", request.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        logger.info("User '{}' logged in successfully", user.getUsername());
        return jwtService.createToken(user);
    }

    public AuthDtos.TokenValidationResponse validate(String authorizationHeader) {
        return jwtService.validate(authorizationHeader);
    }
}
