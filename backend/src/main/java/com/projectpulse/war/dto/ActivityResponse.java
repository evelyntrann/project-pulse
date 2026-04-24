package com.projectpulse.war.dto;

import java.math.BigDecimal;

public record ActivityResponse(
        Long id,
        String category,
        String plannedActivity,
        String description,
        BigDecimal plannedHours,
        BigDecimal actualHours,
        String status
) {}
