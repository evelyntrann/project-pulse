package com.projectpulse.team;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.team.dto.TeamCreateRequest;
import com.projectpulse.team.dto.TeamDetailResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(teamService.createTeam(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getTeam(id)));
    }
}
