package com.projectpulse.user.dto;

public record StudentSearchResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String sectionName,
        String teamName
) {}
