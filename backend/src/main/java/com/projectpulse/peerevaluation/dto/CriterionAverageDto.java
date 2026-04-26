package com.projectpulse.peerevaluation.dto;

import java.math.BigDecimal;

public record CriterionAverageDto(
        Long criterionId,
        String criterionName,
        BigDecimal averageScore,
        BigDecimal maxScore
) {}
