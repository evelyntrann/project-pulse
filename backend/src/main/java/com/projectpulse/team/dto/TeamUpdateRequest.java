package com.projectpulse.team.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamUpdateRequest(

        @NotBlank(message = "Team name is required")
        String name,

        String description,

        String websiteUrl
) {}
