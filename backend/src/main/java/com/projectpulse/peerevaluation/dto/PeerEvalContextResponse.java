package com.projectpulse.peerevaluation.dto;

import java.util.List;

public record PeerEvalContextResponse(
        List<TeamMemberDto> teamMembers,
        List<CriterionDto> criteria,
        List<PeerEvalSummaryDto> existingEvaluations
) {}
