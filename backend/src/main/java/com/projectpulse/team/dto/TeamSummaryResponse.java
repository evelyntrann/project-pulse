package com.projectpulse.team.dto;

import java.util.List;

public record TeamSummaryResponse(
        Long id,
        String name,
        String description,
        String websiteUrl,
        Long sectionId,
        String sectionName,
        List<MemberDto> members,
        List<MemberDto> instructors
) {
    public record MemberDto(
            Long id,
            String firstName,
            String lastName,
            String email
    ) {}
}
