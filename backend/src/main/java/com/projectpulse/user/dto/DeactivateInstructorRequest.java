package com.projectpulse.user.dto;

import jakarta.validation.constraints.NotBlank;

public record DeactivateInstructorRequest(
        @NotBlank(message = "Reason is required")
        String reason
) {}
