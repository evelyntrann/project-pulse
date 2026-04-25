package com.projectpulse.report.dto;

import java.time.LocalDate;
import java.util.List;

public record WARWeekEntryDto(
        LocalDate weekStartDate,
        boolean submitted,
        List<WARActivityDto> activities
) {}
