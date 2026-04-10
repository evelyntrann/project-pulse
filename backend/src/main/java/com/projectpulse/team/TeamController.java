package com.projectpulse.team;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<TeamSummaryResponse>>> findTeams(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.findTeams(sectionId, name)));
    }
}
