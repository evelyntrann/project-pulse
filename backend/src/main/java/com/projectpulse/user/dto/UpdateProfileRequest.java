package com.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        String firstName,

        String lastName,

        @Email(message = "Invalid email address")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
