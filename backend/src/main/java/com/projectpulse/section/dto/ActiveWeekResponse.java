package com.projectpulse.section.dto;

import java.time.LocalDate;

public record ActiveWeekResponse(
        LocalDate weekStartDate,
        boolean isActive
) {}
