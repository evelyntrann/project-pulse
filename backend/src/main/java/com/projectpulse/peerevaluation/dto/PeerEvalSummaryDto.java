package com.projectpulse.peerevaluation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PeerEvalSummaryDto(
        Long id,
        Long evaluateeId,
        List<ScoreSummaryDto> scores,
        String publicComment,
        String privateComment,
        LocalDateTime submittedAt
) {}
