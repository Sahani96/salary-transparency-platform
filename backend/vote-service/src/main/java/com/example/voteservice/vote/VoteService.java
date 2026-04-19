package com.example.voteservice.vote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class VoteService {

    private final VoteRepository repository;
    private final SalaryStatusClient salaryStatusClient;
    private final int threshold;

    public VoteService(
            VoteRepository repository,
            SalaryStatusClient salaryStatusClient,
            @Value("${vote.threshold}") int threshold
    ) {
        this.repository = repository;
        this.salaryStatusClient = salaryStatusClient;
        this.threshold = threshold;
    }

    public VoteDtos.VoteResponse castVote(VoteDtos.CreateVoteRequest request) {
        repository.findBySubmissionIdAndVoterUserId(request.submissionId(), request.voterUserId())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already voted for this submission");
                });

        VoteRecord vote = new VoteRecord();
        vote.setSubmissionId(request.submissionId());
        vote.setVoterUserId(request.voterUserId());
        vote.setVoteType(request.voteType());

        VoteRecord saved = repository.save(vote);
        long upvotes = repository.countBySubmissionIdAndVoteType(request.submissionId(), VoteType.UPVOTE);
        if (upvotes >= threshold) {
            salaryStatusClient.approve(request.submissionId());
        }
        return toResponse(saved);
    }

    public VoteDtos.VoteCountResponse counts(UUID submissionId) {
        long upvotes = repository.countBySubmissionIdAndVoteType(submissionId, VoteType.UPVOTE);
        long downvotes = repository.countBySubmissionIdAndVoteType(submissionId, VoteType.DOWNVOTE);
        return new VoteDtos.VoteCountResponse(submissionId, upvotes, downvotes, upvotes - downvotes);
    }

    private VoteDtos.VoteResponse toResponse(VoteRecord vote) {
        return new VoteDtos.VoteResponse(
                vote.getId(),
                vote.getSubmissionId(),
                vote.getVoterUserId(),
                vote.getVoteType(),
                vote.getCreatedAt()
        );
    }
}
