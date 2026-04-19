package com.example.salaryservice.submission;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class SalarySubmissionService {

    private final SalarySubmissionRepository repository;

    public SalarySubmissionService(SalarySubmissionRepository repository) {
        this.repository = repository;
    }

    public SubmissionDtos.SubmissionResponse create(SubmissionDtos.CreateSubmissionRequest request) {
        SalarySubmission submission = new SalarySubmission();
        submission.setJobTitle(request.jobTitle().trim());
        submission.setCompany(request.company().trim());
        submission.setCountry(request.country().trim());
        submission.setCity(request.city());
        submission.setExperienceLevel(request.experienceLevel());
        submission.setYearsOfExperience(request.yearsOfExperience());
        submission.setBaseSalary(request.baseSalary());
        submission.setCurrency(request.currency().trim().toUpperCase());
        submission.setEmploymentType(request.employmentType());
        submission.setAnonymize(request.anonymize());
        submission.setTechStack(request.techStack());
        submission.setStatus(SubmissionStatus.PENDING);

        return toResponse(repository.save(submission));
    }

    public SubmissionDtos.SubmissionResponse get(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }

    public SubmissionDtos.SubmissionResponse updateStatus(UUID id, SubmissionDtos.UpdateStatusRequest request) {
        SalarySubmission submission = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        submission.setStatus(request.status());
        return toResponse(repository.save(submission));
    }

    private SubmissionDtos.SubmissionResponse toResponse(SalarySubmission submission) {
        return new SubmissionDtos.SubmissionResponse(
                submission.getId(),
                submission.getJobTitle(),
                submission.getCompany(),
                submission.getCountry(),
                submission.getCity(),
                submission.getExperienceLevel(),
                submission.getYearsOfExperience(),
                submission.getBaseSalary(),
                submission.getCurrency(),
                submission.getEmploymentType(),
                submission.isAnonymize(),
                submission.getStatus(),
                submission.getTechStack(),
                submission.getSubmittedAt()
        );
    }
}
