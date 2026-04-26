package com.projectpulse.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeerEvalSectionReportResponse(
        LocalDate weekStartDate,
        String sectionName,
        BigDecimal maxGrade,
        List<StudentPeerEvalEntryDto> students,
        List<String> nonSubmitters
) {}
