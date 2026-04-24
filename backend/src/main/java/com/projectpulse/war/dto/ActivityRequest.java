package com.projectpulse.war.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ActivityRequest(
        @NotBlank String category,
        @NotBlank String plannedActivity,
        String description,
        @NotNull @DecimalMin("0") BigDecimal plannedHours,
        @DecimalMin("0") BigDecimal actualHours,
        String status
) {}
