package com.projectpulse.rubric.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CriterionRequest(

        @NotBlank(message = "Criterion name is required")
        String name,

        @NotBlank(message = "Criterion description is required")
        String description,

        @NotNull(message = "Max score is required")
        @DecimalMin(value = "0.01", message = "Max score must be a positive number")
        BigDecimal maxScore,

        @NotNull(message = "Order index is required")
        Integer orderIndex
) {}
