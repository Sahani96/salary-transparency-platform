package com.example.voteservice.vote;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public final class VoteDtos {

    private VoteDtos() {
    }

    public record CreateVoteRequest(
            @NotNull UUID submissionId,
            @NotNull UUID voterUserId,
            @NotNull VoteType voteType
    ) {
    }

    public record VoteResponse(
            UUID id,
            UUID submissionId,
            UUID voterUserId,
            VoteType voteType,
            LocalDateTime createdAt
    ) {
    }

    public record VoteCountResponse(
            UUID submissionId,
            long upvotes,
            long downvotes,
            long score
    ) {
    }
}
