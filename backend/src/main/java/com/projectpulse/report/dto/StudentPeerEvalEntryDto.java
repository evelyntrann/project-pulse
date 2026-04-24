package com.projectpulse.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentPeerEvalEntryDto(
        Long studentId,
        String studentName,
        BigDecimal grade,
        List<EvaluatorEntryDto> evaluations
) {}
