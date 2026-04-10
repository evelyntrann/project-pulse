package com.projectpulse.team.dto;

import java.util.List;

public record TeamDetailResponse(
        Long id,
        String name,
        String description,
        String websiteUrl,
        SectionDto section,
        List<MemberDto> members,
        List<MemberDto> instructors
) {
    public record SectionDto(
            Long id,
            String name
    ) {}

    public record MemberDto(
            Long id,
            String firstName,
            String lastName,
            String email
    ) {}
}
