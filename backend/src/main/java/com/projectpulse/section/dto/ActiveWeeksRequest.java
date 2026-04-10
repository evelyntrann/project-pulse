package com.projectpulse.section.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ActiveWeeksRequest(

        @NotNull(message = "Weeks list is required")
        @Valid
        List<WeekDto> weeks
) {
    public record WeekDto(
            @NotNull(message = "Week start date is required")
            LocalDate weekStartDate,

            @NotNull(message = "isActive is required")
            Boolean isActive
    ) {}
}
