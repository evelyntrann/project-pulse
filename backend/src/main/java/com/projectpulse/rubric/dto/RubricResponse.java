package com.projectpulse.rubric.dto;

import java.util.List;

public record RubricResponse(
        Long id,
        String name,
        List<CriterionResponse> criteria
) {}
