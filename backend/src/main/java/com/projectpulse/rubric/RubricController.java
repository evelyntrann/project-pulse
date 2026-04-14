package com.projectpulse.rubric;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.rubric.dto.RubricCreateRequest;
import com.projectpulse.rubric.dto.RubricResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RubricResponse>> createRubric(
            @Valid @RequestBody RubricCreateRequest request,
            Authentication auth) {
        Long adminId = Long.parseLong(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rubricService.createRubric(request, adminId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RubricResponse>>> listRubrics() {
        return ResponseEntity.ok(ApiResponse.ok(rubricService.listRubrics()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RubricResponse>> getRubric(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rubricService.getRubric(id)));
    }
}
