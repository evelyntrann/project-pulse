package com.projectpulse.section;

import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<SectionSummaryResponse> findSections(String name) {
        return sectionRepository.findByNameContaining(name).stream()
                .map(section -> new SectionSummaryResponse(
                        section.getId(),
                        section.getName(),
                        section.getStartDate(),
                        section.getEndDate(),
                        section.getTeams().stream()
                                .map(t -> t.getName())
                                .sorted()
                                .toList()
                ))
                .toList();
    }

    public SectionDetailResponse getSection(Long id) {
        SectionEntity section = sectionRepository.findByIdWithTeams(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        List<SectionDetailResponse.TeamDto> teamDtos = section.getTeams().stream()
                .map(team -> new SectionDetailResponse.TeamDto(
                        team.getId(),
                        team.getName(),
                        team.getDescription(),
                        team.getWebsiteUrl(),
                        List.of(), // populated when Angel/Micah build student assignment
                        List.of()  // populated when Angel/Micah build instructor assignment
                ))
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();

        return new SectionDetailResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                section.getRubricId(),
                teamDtos,
                List.of(), // unassigned students — populated when Micah builds UC-25
                List.of()  // unassigned instructors — populated when Angel builds UC-18
        );
    }
}
