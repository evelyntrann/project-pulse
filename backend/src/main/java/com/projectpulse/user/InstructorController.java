package com.projectpulse.user;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.team.TeamEntity;
import com.projectpulse.team.TeamRepository;
import com.projectpulse.user.dto.InstructorDetailResponse;
import com.projectpulse.user.dto.InstructorSearchResponse;
import com.projectpulse.user.dto.InstructorSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public InstructorController(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    // UC-21: search instructors
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InstructorSearchResponse>>> searchInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Boolean active) {
        String fn = (firstName != null && !firstName.isBlank()) ? firstName.trim() : null;
        String ln = (lastName  != null && !lastName.isBlank())  ? lastName.trim()  : null;
        String tn = (teamName  != null && !teamName.isBlank())  ? teamName.trim()  : null;

        List<InstructorSearchResponse> results = userRepository
                .searchInstructors(fn, ln, tn, active)
                .stream()
                .map(p -> new InstructorSearchResponse(
                        p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(),
                        Boolean.TRUE.equals(p.getIsActive()),
                        p.getTeamNames()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    // UC-22: view instructor details
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstructorDetailResponse>> getInstructor(@PathVariable Long id) {
        UserEntity instructor = userRepository.findById(id)
                .filter(u -> "INSTRUCTOR".equals(u.getRole()))
                .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

        List<TeamEntity> teams = teamRepository.findByInstructorId(id);

        // Group teams by section, preserving alphabetical order
        record SectionKey(Long id, String name) {}
        var grouped = new java.util.LinkedHashMap<SectionKey, List<InstructorDetailResponse.TeamDto>>();
        for (TeamEntity t : teams) {
            var key = new SectionKey(t.getSection().getId(), t.getSection().getName());
            grouped.computeIfAbsent(key, k -> new ArrayList<>())
                   .add(new InstructorDetailResponse.TeamDto(t.getId(), t.getName()));
        }

        List<InstructorDetailResponse.SectionTeamsDto> supervisedTeams = grouped.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .map(e -> new InstructorDetailResponse.SectionTeamsDto(
                        e.getKey().id(), e.getKey().name(), e.getValue()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(new InstructorDetailResponse(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                instructor.isActive(),
                supervisedTeams)));
    }

    // Used by UC-19 assign view to populate the instructor dropdown
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InstructorSummaryResponse>>> listInstructors() {
        List<InstructorSummaryResponse> instructors = userRepository
                .findAllByRoleAndIsActiveTrue("INSTRUCTOR")
                .stream()
                .map(u -> new InstructorSummaryResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .sorted(Comparator.comparing(InstructorSummaryResponse::lastName)
                        .thenComparing(InstructorSummaryResponse::firstName))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(instructors));
    }
}
