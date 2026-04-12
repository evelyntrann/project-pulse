package com.projectpulse.team.dto;

public record TeamTransferResponse(
        Long teamId,
        String teamName,
        Long fromSectionId,
        String fromSectionName,
        Long toSectionId,
        String toSectionName,
        int studentsTransferred
) {}
