package com.projectpulse.team;

import com.projectpulse.section.SectionRepository;
import com.projectpulse.team.dto.TeamCreateRequest;
import com.projectpulse.team.dto.TeamDetailResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import com.projectpulse.team.dto.TeamUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;

    public TeamService(TeamRepository teamRepository, SectionRepository sectionRepository) {
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public TeamDetailResponse createTeam(TeamCreateRequest request) {
        if (teamRepository.existsByName(request.name())) {
            throw new DuplicateTeamException("Team '" + request.name() + "' already exists");
        }

        var section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        TeamEntity team = new TeamEntity();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());
        team.setSection(section);

        TeamEntity saved = teamRepository.save(team);

        return new TeamDetailResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getWebsiteUrl(),
                new TeamDetailResponse.SectionDto(section.getId(), section.getName()),
                List.of(),
                List.of()
        );
    }

    public TeamDetailResponse getTeam(Long id) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        return new TeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getWebsiteUrl(),
                new TeamDetailResponse.SectionDto(
                        team.getSection().getId(),
                        team.getSection().getName()
                ),
                List.of(), // members — populated when UC-12 is built
                List.of()  // instructors — populated when UC-19 is built
        );
    }

    @Transactional
    public TeamDetailResponse updateTeam(Long id, TeamUpdateRequest request) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        if (teamRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateTeamException("Team '" + request.name() + "' already exists");
        }

        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());

        TeamEntity saved = teamRepository.save(team);

        return new TeamDetailResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getWebsiteUrl(),
                new TeamDetailResponse.SectionDto(saved.getSection().getId(), saved.getSection().getName()),
                List.of(),
                List.of()
        );
    }

    public List<TeamSummaryResponse> findTeams(Long sectionId, String name) {
        // TODO: add instructorId filter once Angel builds user/instructor assignment (UC-19)
        return teamRepository.findByFilters(sectionId, name).stream()
                .map(team -> new TeamSummaryResponse(
                        team.getId(),
                        team.getName(),
                        team.getDescription(),
                        team.getWebsiteUrl(),
                        team.getSection().getId(),
                        team.getSection().getName(),
                        List.of(), // members — populated when UC-12 is built
                        List.of()  // instructors — populated when UC-19 is built
                ))
                .toList();
    }
}
