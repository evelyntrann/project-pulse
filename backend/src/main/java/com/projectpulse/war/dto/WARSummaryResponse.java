package com.projectpulse.war.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WARSummaryResponse(
        Long id,
        LocalDate weekStartDate,
        String status,
        LocalDateTime submittedAt
) {}
