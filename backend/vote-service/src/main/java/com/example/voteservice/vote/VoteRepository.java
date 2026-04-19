package com.example.voteservice.vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<VoteRecord, UUID> {

    Optional<VoteRecord> findBySubmissionIdAndVoterUserId(UUID submissionId, UUID voterUserId);

    long countBySubmissionIdAndVoteType(UUID submissionId, VoteType voteType);
}
