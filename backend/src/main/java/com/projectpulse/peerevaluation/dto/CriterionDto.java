package com.projectpulse.peerevaluation.dto;

import java.math.BigDecimal;

public record CriterionDto(Long id, String name, String description, BigDecimal maxScore) {}
