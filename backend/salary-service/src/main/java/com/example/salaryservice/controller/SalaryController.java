package com.example.salaryservice.controller;

import com.example.salaryservice.dto.SalarySubmissionRequest;
import com.example.salaryservice.dto.SalarySubmissionResponse;
import com.example.salaryservice.entity.SalarySubmission;
import com.example.salaryservice.repository.SalarySubmissionRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryController.class);
    private final SalarySubmissionRepository repository;

    public SalaryController(SalarySubmissionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> submitSalary(@Valid @RequestBody SalarySubmissionRequest request) {
        SalarySubmission submission = SalarySubmission.builder()
                .jobTitle(request.getJobTitle())
                .company(request.getCompany())
                .country(request.getCountry())
                .city(request.getCity())
                .experienceLevel(request.getExperienceLevel())
                .yearsOfExperience(request.getYearsOfExperience())
                .baseSalary(request.getBaseSalary())
                .currency(request.getCurrency() != null ? request.getCurrency() : "LKR")
                .employmentType(request.getEmploymentType() != null ? request.getEmploymentType() : "FULL_TIME")
                .anonymize(request.getAnonymize() != null ? request.getAnonymize() : false)
                .techStack(request.getTechStack())
                .status("PENDING")
                .build();

        submission = repository.save(submission);
        logger.info("Salary submission created with id: {} and status: PENDING", submission.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(submission));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubmission(@PathVariable UUID id) {
        return repository.findById(id)
                .map(sub -> ResponseEntity.ok(toResponse(sub)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || (!newStatus.equals("APPROVED") && !newStatus.equals("REJECTED"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status. Must be APPROVED or REJECTED"));
        }

        return repository.findById(id)
                .map(sub -> {
                    sub.setStatus(newStatus);
                    repository.save(sub);
                    logger.info("Submission {} status updated to {}", id, newStatus);
                    return ResponseEntity.ok(toResponse(sub));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private SalarySubmissionResponse toResponse(SalarySubmission entity) {
        return SalarySubmissionResponse.builder()
                .id(entity.getId().toString())
                .jobTitle(entity.getJobTitle())
                .company(entity.getAnonymize() ? "Anonymous" : entity.getCompany())
                .country(entity.getCountry())
                .city(entity.getAnonymize() ? entity.getCountry() : entity.getCity())
                .experienceLevel(entity.getExperienceLevel())
                .yearsOfExperience(entity.getYearsOfExperience())
                .baseSalary(entity.getBaseSalary())
                .currency(entity.getCurrency())
                .employmentType(entity.getEmploymentType())
                .anonymize(entity.getAnonymize())
                .status(entity.getStatus())
                .techStack(entity.getTechStack())
                .submittedAt(entity.getSubmittedAt())
                .build();
    }
}
