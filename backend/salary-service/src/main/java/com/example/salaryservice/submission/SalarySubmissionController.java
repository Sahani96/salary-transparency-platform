package com.example.salaryservice.submission;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/submissions")
public class SalarySubmissionController {

    private final SalarySubmissionService service;

    public SalarySubmissionController(SalarySubmissionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubmissionDtos.SubmissionResponse> create(
            @Valid @RequestBody SubmissionDtos.CreateSubmissionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDtos.SubmissionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SubmissionDtos.SubmissionResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody SubmissionDtos.UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }
}
