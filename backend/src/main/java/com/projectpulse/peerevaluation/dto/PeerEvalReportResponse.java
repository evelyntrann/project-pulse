package com.projectpulse.peerevaluation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvalReportResponse(
        LocalDate weekStartDate,
        String evaluateeName,
        int evaluatorCount,
        List<CriterionAverageDto> criterionAverages,
        List<String> publicComments,
        BigDecimal grade,
        BigDecimal maxGrade
) {}
