package com.projectpulse.report.dto;

import java.time.LocalDate;
import java.util.List;

public record WARTeamReportResponse(
        LocalDate weekStartDate,
        String teamName,
        List<WARStudentEntryDto> students,
        List<String> nonSubmitters
) {}
