package com.projectpulse.auth.dto;

public record LoginResponse(
        String token,
        UserDto user
) {
    public record UserDto(
            Long id,
            String email,
            String firstName,
            String lastName,
            String role
    ) {}
}
