package com.projectpulse.registration.dto;

public record RegisterResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
