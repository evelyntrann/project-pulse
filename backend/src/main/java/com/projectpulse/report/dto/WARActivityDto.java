package com.projectpulse.report.dto;

import java.math.BigDecimal;

public record WARActivityDto(
        String category,
        String plannedActivity,
        String description,
        BigDecimal plannedHours,
        BigDecimal actualHours,
        String status
) {}
