package com.projectpulse.peerevaluation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PeerEvalSubmitRequest(
        @NotNull LocalDate weekStartDate,
        @NotNull @Size(min = 1) List<@Valid EvaluationEntry> evaluations
) {
    public record EvaluationEntry(
            @NotNull Long evaluateeId,
            @NotNull List<@Valid ScoreEntry> scores,
            String publicComment,
            String privateComment
    ) {}

    public record ScoreEntry(
            @NotNull Long criterionId,
            @NotNull @Min(1) Integer score
    ) {}
}
