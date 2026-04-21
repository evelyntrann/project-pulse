package com.projectpulse.invitation.dto;

import jakarta.validation.constraints.NotNull;

public record InviteLinkRequest(
        @NotNull(message = "Section ID is required")
        Long sectionId
) {}
