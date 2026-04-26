package com.projectpulse.user.dto;

public record InstructorSearchResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active,
        String teamNames
) {}
