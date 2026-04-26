package com.projectpulse.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record SectionCreateRequest(

        @NotBlank(message = "Section name is required")
        String name,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        Long rubricId,

        Boolean isActive,

        DayOfWeek warWeeklyDueDay,

        LocalTime warDueTime,

        DayOfWeek peerEvaluationWeeklyDueDay,

        LocalTime peerEvaluationDueTime
) {}
