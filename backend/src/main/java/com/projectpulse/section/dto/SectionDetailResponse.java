package com.projectpulse.section.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SectionDetailResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long rubricId,
        boolean isActive,
        DayOfWeek warWeeklyDueDay,
        LocalTime warDueTime,
        DayOfWeek peerEvaluationWeeklyDueDay,
        LocalTime peerEvaluationDueTime,
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
