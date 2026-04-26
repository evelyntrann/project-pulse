package com.projectpulse.invitation.dto;

import java.time.LocalDateTime;

public record InviteLinkResponse(
        String shareableLink,
        LocalDateTime expiresAt
) {}
