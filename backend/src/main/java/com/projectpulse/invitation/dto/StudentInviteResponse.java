package com.projectpulse.invitation.dto;

import java.util.List;

public record StudentInviteResponse(
        int sentCount,
        List<String> emails
) {}
