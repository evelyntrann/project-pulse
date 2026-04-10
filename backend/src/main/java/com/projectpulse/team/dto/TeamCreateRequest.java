package com.projectpulse.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamCreateRequest(

        @NotBlank(message = "Team name is required")
        String name,

        String description,

        String websiteUrl,

        @NotNull(message = "Section ID is required")
        Long sectionId
) {}
