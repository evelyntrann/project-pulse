package com.projectpulse.team;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.team.dto.AssignStudentsRequest;
import com.projectpulse.team.dto.TeamCreateRequest;
import com.projectpulse.team.dto.TeamDetailResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import com.projectpulse.team.dto.TeamTransferRequest;
import com.projectpulse.team.dto.TeamTransferResponse;
import com.projectpulse.team.dto.TeamUpdateRequest;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.updateTeam(id, request)));
    }

    @PostMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> assignStudents(
            @PathVariable Long id,
            @Valid @RequestBody AssignStudentsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.assignStudents(id, request)));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> removeStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.removeStudent(id, studentId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignInstructorToTeam(
            @PathVariable Long id,
            @PathVariable Long instructorId) {
        teamService.assignInstructorToTeam(id, instructorId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{id}/section")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamTransferResponse>> transferTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.transferTeamToAnotherSection(id, request.sectionId())));
    }
}
