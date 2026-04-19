package com.example.voteservice.vote;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/votes")
public class VoteController {

    private final VoteService service;

    public VoteController(VoteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VoteDtos.VoteResponse> castVote(@Valid @RequestBody VoteDtos.CreateVoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.castVote(request));
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<VoteDtos.VoteCountResponse> getCounts(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(service.counts(submissionId));
    }
}
