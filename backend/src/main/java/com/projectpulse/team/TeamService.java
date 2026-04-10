package com.projectpulse.team;

import com.projectpulse.team.dto.TeamSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
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
