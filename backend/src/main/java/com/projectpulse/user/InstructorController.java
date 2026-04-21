package com.projectpulse.user;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.user.dto.InstructorSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final UserRepository userRepository;

    public InstructorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
