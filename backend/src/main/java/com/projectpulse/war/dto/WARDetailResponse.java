package com.projectpulse.war.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WARDetailResponse(
        Long id,
        LocalDate weekStartDate,
        String status,
        LocalDateTime submittedAt,
        List<ActivityResponse> activities
) {}
