package com.example.voteservice.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<VoteRecord, UUID> {

    Optional<VoteRecord> findBySubmissionIdAndVoterUserId(UUID submissionId, UUID voterUserId);

    long countBySubmissionIdAndVoteType(UUID submissionId, VoteType voteType);

    @Modifying
    @Query(value = """
            INSERT INTO community.votes (id, submission_id, voter_user_id, vote_type, created_at)
            VALUES (gen_random_uuid(), :submissionId, :voterUserId, :voteType, NOW())
            ON CONFLICT ON CONSTRAINT uk_votes_submission_user
            DO UPDATE SET vote_type = :voteType
            """, nativeQuery = true)
    void upsertVote(@Param("submissionId") UUID submissionId,
                    @Param("voterUserId") UUID voterUserId,
                    @Param("voteType") String voteType);
}
