package com.projectpulse.invitation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StudentInviteRequest(

        @NotNull(message = "Section ID is required")
        Long sectionId,

        @NotEmpty(message = "At least one email is required")
        List<String> emails,

        String customMessage
) {}
