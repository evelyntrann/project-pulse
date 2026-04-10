package com.projectpulse.section;

import com.projectpulse.section.dto.ActiveWeekResponse;
import com.projectpulse.section.dto.ActiveWeeksRequest;
import com.projectpulse.section.dto.SectionCreateRequest;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionStudentResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import com.projectpulse.section.dto.SectionUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ActiveWeekRepository activeWeekRepository;

    public SectionService(SectionRepository sectionRepository, ActiveWeekRepository activeWeekRepository) {
        this.sectionRepository = sectionRepository;
        this.activeWeekRepository = activeWeekRepository;
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

    @Transactional
    public SectionDetailResponse updateSection(Long id, SectionUpdateRequest request) {
        SectionEntity section = sectionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        if (!section.getName().equals(request.name()) && sectionRepository.existsByName(request.name())) {
            throw new DuplicateSectionException("Section '" + request.name() + "' already exists");
        }

        // TODO: validate rubricId exists once Angel builds the rubric package (UC-1)

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

    public List<ActiveWeekResponse> getActiveWeeks(Long sectionId) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        return activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId).stream()
                .map(w -> new ActiveWeekResponse(w.getWeekStartDate(), w.isActive()))
                .toList();
    }

    @Transactional
    public List<ActiveWeekResponse> setActiveWeeks(Long sectionId, ActiveWeeksRequest request) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        activeWeekRepository.deleteBySectionId(sectionId);

        List<ActiveWeekEntity> entities = request.weeks().stream()
                .map(w -> {
                    ActiveWeekEntity entity = new ActiveWeekEntity();
                    entity.setSectionId(sectionId);
                    entity.setWeekStartDate(w.weekStartDate());
                    entity.setActive(w.isActive());
                    return entity;
                })
                .toList();

        activeWeekRepository.saveAll(entities);

        return entities.stream()
                .map(w -> new ActiveWeekResponse(w.getWeekStartDate(), w.isActive()))
                .toList();
    }

    public SectionDetailResponse getSection(Long id) {
        SectionEntity section = sectionRepository.findByIdWithTeamsAndStudents(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        // Collect all student IDs already on a team in this section
        var assignedStudentIds = section.getTeams().stream()
                .flatMap(t -> t.getStudents().stream())
                .map(s -> s.getId())
                .collect(java.util.stream.Collectors.toSet());

        List<SectionDetailResponse.TeamDto> teamDtos = section.getTeams().stream()
                .map(team -> new SectionDetailResponse.TeamDto(
                        team.getId(),
                        team.getName(),
                        team.getDescription(),
                        team.getWebsiteUrl(),
                        team.getStudents().stream()
                                .map(s -> new SectionDetailResponse.UserDto(
                                        s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                                .toList(),
                        List.of() // instructors — populated when UC-19 is built
                ))
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();

        List<SectionDetailResponse.UserDto> unassignedStudents = section.getEnrolledStudents().stream()
                .filter(s -> !assignedStudentIds.contains(s.getId()))
                .map(s -> new SectionDetailResponse.UserDto(
                        s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                .toList();

        return new SectionDetailResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                section.getRubricId(),
                teamDtos,
                unassignedStudents,
                List.of() // unassigned instructors — populated when UC-19 is built
        );
    }

    public List<SectionStudentResponse> getEnrolledStudents(Long sectionId) {
        SectionEntity section = sectionRepository.findByIdWithEnrolledStudents(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        return section.getEnrolledStudents().stream()
                .map(s -> new SectionStudentResponse(
                        s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                .toList();
    }
}
