package com.projectpulse.section;

import com.projectpulse.section.dto.SectionCreateRequest;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public SectionDetailResponse createSection(SectionCreateRequest request) {
        if (sectionRepository.existsByName(request.name())) {
            throw new DuplicateSectionException("Section '" + request.name() + "' already exists");
        }

        // TODO: validate rubricId exists once Angel builds the rubric package (UC-1)

        SectionEntity section = new SectionEntity();
        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubricId(request.rubricId());

        SectionEntity saved = sectionRepository.save(section);

        return new SectionDetailResponse(
                saved.getId(),
                saved.getName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getRubricId(),
                List.of(),
                List.of(),
                List.of()
        );
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
