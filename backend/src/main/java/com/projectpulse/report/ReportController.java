package com.projectpulse.report;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.report.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Returns sections the logged-in instructor is enrolled in — used to populate the section picker.
    @GetMapping("/instructor/sections")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<InstructorSectionDto>>> getInstructorSections(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getInstructorSections(currentUserId(auth))));
    }

    // Available weeks for a section — mirrors the student available-weeks endpoint but section-scoped.
    @GetMapping("/peer-evaluations/sections/{sectionId}/available-weeks")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getSectionAvailableWeeks(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getAvailableWeeksForSection(sectionId)));
    }

    // UC-31: section-wide peer eval report for one week.
    @GetMapping("/peer-evaluations/sections/{sectionId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<PeerEvalSectionReportResponse>> getPeerEvalSectionReport(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getPeerEvalSectionReport(sectionId, weekStartDate)));
    }

    // UC-32: teams the logged-in instructor is assigned to.
    @GetMapping("/war/instructor/teams")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<InstructorTeamDto>>> getInstructorTeams(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getInstructorTeams(currentUserId(auth))));
    }

    // UC-32: the team the logged-in student belongs to.
    @GetMapping("/war/my-team")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<InstructorTeamDto>> getStudentTeam(Authentication auth) {
        InstructorTeamDto team = reportService.getStudentTeam(currentUserId(auth))
                .orElseThrow(() -> new java.util.NoSuchElementException("You are not assigned to a team yet"));
        return ResponseEntity.ok(ApiResponse.ok(team));
    }

    // UC-32: active weeks available for a team.
    @GetMapping("/war/teams/{teamId}/available-weeks")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getTeamAvailableWeeks(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getAvailableWeeksForTeam(teamId)));
    }

    // UC-32: WAR report for all students on a team for one week.
    @GetMapping("/war/teams/{teamId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponse<WARTeamReportResponse>> getWARTeamReport(
            @PathVariable Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication auth) {
        boolean isInstructor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTRUCTOR"));
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getWARTeamReport(teamId, weekStartDate, currentUserId(auth), isInstructor)));
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
