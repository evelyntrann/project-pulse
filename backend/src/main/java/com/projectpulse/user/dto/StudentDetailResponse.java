package com.projectpulse.user.dto;

public record StudentDetailResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String sectionName,
        String teamName
        // peerEvaluations and WARs will be added by Micah (UC-27, UC-28)
) {}
