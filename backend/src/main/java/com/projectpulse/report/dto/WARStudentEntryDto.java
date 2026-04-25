package com.projectpulse.report.dto;

import java.util.List;

public record WARStudentEntryDto(
        Long studentId,
        String studentName,
        List<WARActivityDto> activities
) {}
