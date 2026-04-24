package com.projectpulse.report.dto;

public record EvaluatorEntryDto(
        String evaluatorName,
        int totalScore,
        String publicComment,
        String privateComment
) {}
