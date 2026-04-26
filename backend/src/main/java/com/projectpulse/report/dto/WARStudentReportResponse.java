package com.projectpulse.report.dto;

import java.util.List;

public record WARStudentReportResponse(
        Long studentId,
        String studentName,
        List<WARWeekEntryDto> weeks
) {}
