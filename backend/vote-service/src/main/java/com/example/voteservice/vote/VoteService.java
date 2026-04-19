package com.example.voteservice.vote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VoteService {

    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);

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

    @Transactional
    public VoteDtos.VoteResponse castVote(VoteDtos.CreateVoteRequest request) {
        logger.info("Casting {} for submission {} by user {}",
                request.voteType(), request.submissionId(), request.voterUserId());

        repository.upsertVote(request.submissionId(), request.voterUserId(), request.voteType().name());

        VoteRecord saved = repository
                .findBySubmissionIdAndVoterUserId(request.submissionId(), request.voterUserId())
                .orElseThrow(() -> {
                    logger.error("Vote not found after upsert for submission {} user {}",
                            request.submissionId(), request.voterUserId());
                    return new IllegalStateException("Vote not found after upsert");
                });

        long upvotes = repository.countBySubmissionIdAndVoteType(request.submissionId(), VoteType.UPVOTE);
        long downvotes = repository.countBySubmissionIdAndVoteType(request.submissionId(), VoteType.DOWNVOTE);
        logger.info("Submission {} vote counts: upvotes={}, downvotes={}", request.submissionId(), upvotes, downvotes);

        if (upvotes >= threshold) {
            logger.info("Submission {} reached threshold ({}), approving", request.submissionId(), threshold);
            try {
                salaryStatusClient.approve(request.submissionId());
            } catch (Exception e) {
                logger.error("Failed to approve submission {} in salary-service: {}", request.submissionId(), e.getMessage(), e);
                throw e;
            }
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
