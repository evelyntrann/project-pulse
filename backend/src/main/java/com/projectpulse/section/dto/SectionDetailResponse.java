package com.projectpulse.section.dto;

import java.time.LocalDate;
import java.util.List;

public record SectionDetailResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long rubricId,
        List<TeamDto> teams,
        List<UserDto> unassignedStudents,
        List<UserDto> unassignedInstructors
) {
    public record TeamDto(
            Long id,
            String name,
            String description,
            String websiteUrl,
            List<UserDto> members,
            List<UserDto> instructors
    ) {}

    public record UserDto(
            Long id,
            String firstName,
            String lastName,
            String email
    ) {}
}
