package com.projectpulse.report;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.report.dto.InstructorSectionDto;
import com.projectpulse.report.dto.PeerEvalSectionReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Returns sections the logged-in instructor is enrolled in — used to populate the section picker.
    @GetMapping("/instructor/sections")
    public ResponseEntity<ApiResponse<List<InstructorSectionDto>>> getInstructorSections(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getInstructorSections(currentUserId(auth))));
    }

    // Available weeks for a section — mirrors the student available-weeks endpoint but section-scoped.
    @GetMapping("/peer-evaluations/sections/{sectionId}/available-weeks")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getSectionAvailableWeeks(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getAvailableWeeksForSection(sectionId)));
    }

    // UC-31: section-wide peer eval report for one week.
    @GetMapping("/peer-evaluations/sections/{sectionId}")
    public ResponseEntity<ApiResponse<PeerEvalSectionReportResponse>> getPeerEvalSectionReport(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getPeerEvalSectionReport(sectionId, weekStartDate)));
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
