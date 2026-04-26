package com.projectpulse.team.dto;

import jakarta.validation.constraints.NotNull;

public record TeamTransferRequest(
        @NotNull(message = "New section ID is required")
        Long sectionId
) {}
