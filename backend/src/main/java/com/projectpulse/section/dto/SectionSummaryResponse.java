package com.projectpulse.section.dto;

import java.time.LocalDate;
import java.util.List;

public record SectionSummaryResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        List<String> teamNames
) {}
