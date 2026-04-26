package com.projectpulse.invitation.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InstructorInviteRequest(

        @NotEmpty(message = "At least one email is required")
        List<String> emails,

        String customMessage
) {}
