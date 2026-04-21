package com.projectpulse.user.dto;

import java.util.List;

public record InstructorDetailResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active,
        List<SectionTeamsDto> supervisedTeams
) {
    public record SectionTeamsDto(
            Long sectionId,
            String sectionName,
            List<TeamDto> teams
    ) {}

    public record TeamDto(
            Long id,
            String name
    ) {}
}
