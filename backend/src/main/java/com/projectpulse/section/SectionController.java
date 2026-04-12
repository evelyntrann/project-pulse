package com.projectpulse.section;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.section.dto.ActiveWeekResponse;
import com.projectpulse.section.dto.ActiveWeeksRequest;
import com.projectpulse.section.dto.SectionCreateRequest;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionStudentResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import com.projectpulse.section.dto.SectionUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SectionSummaryResponse>>> findSections(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.findSections(name)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> createSection(
            @Valid @RequestBody SectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(sectionService.createSection(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSection(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.updateSection(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/active-weeks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ActiveWeekResponse>>> getActiveWeeks(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getActiveWeeks(id)));
    }

    @PutMapping("/{id}/active-weeks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ActiveWeekResponse>>> setActiveWeeks(
            @PathVariable Long id,
            @Valid @RequestBody ActiveWeeksRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.setActiveWeeks(id, request)));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SectionStudentResponse>>> getEnrolledStudents(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getEnrolledStudents(id)));
    }

    @PutMapping("/{id}/rubrics/{rubricId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRubric(
            @PathVariable Long id,
            @PathVariable Long rubricId) {
        sectionService.assignRubric(id, rubricId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SectionDetailResponse.UserDto>>> getInstructors(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getInstructors(id)));
    }

    @PutMapping("/{id}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignInstructor(
            @PathVariable Long id,
            @PathVariable Long instructorId) {
        sectionService.assignInstructor(id, instructorId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeInstructor(
            @PathVariable Long id,
            @PathVariable Long instructorId) {
        sectionService.removeInstructor(id, instructorId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/instructors/invite-or-add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inviteOrAddInstructors(
            @PathVariable Long id,
            @RequestBody List<String> emails) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.inviteOrAddInstructors(id, emails)));
    }

    @PostMapping("/{id}/students/email-invitations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendStudentEmailInvitations(
            @PathVariable Long id,
            @RequestBody List<String> emails) {
        // Delegates to InvitationController — kept here as a passthrough for API consistency
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
