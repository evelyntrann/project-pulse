package com.projectpulse.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvalWeekEntryDto(
        LocalDate weekStartDate,
        BigDecimal grade,
        List<EvaluatorEntryDto> evaluations
) {}
