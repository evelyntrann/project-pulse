package com.projectpulse.user;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.user.dto.InstructorSearchResponse;
import com.projectpulse.user.dto.InstructorSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final UserRepository userRepository;

    public InstructorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // UC-21: search instructors with optional filters
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

    // Used internally by UC-19 assign view to populate the instructor dropdown
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InstructorSummaryResponse>>> listInstructors() {
        List<InstructorSummaryResponse> instructors = userRepository
                .findAllByRoleAndIsActiveTrue("INSTRUCTOR")
                .stream()
                .map(u -> new InstructorSummaryResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .sorted(java.util.Comparator.comparing(InstructorSummaryResponse::lastName)
                        .thenComparing(InstructorSummaryResponse::firstName))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(instructors));
    }
}
