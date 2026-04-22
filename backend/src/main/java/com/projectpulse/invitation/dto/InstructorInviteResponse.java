package com.projectpulse.invitation.dto;

import java.util.List;

public record InstructorInviteResponse(
        int count,
        List<String> emails
) {}
