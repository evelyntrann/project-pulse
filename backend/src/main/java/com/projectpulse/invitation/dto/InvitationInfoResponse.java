package com.projectpulse.invitation.dto;

import java.time.LocalDateTime;

public record InvitationInfoResponse(
        String role,
        String sectionName,
        LocalDateTime expiresAt
) {}
