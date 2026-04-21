package com.projectpulse.user.dto;

public record InstructorSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {}
