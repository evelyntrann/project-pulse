package com.projectpulse.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record PeerEvalStudentReportResponse(
        Long studentId,
        String studentName,
        BigDecimal maxGrade,
        List<PeerEvalWeekEntryDto> weeks
) {}
