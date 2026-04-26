package com.projectpulse.peerevaluation;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.peerevaluation.dto.PeerEvalContextResponse;
import com.projectpulse.peerevaluation.dto.PeerEvalReportResponse;
import com.projectpulse.peerevaluation.dto.PeerEvalSubmitRequest;
import com.projectpulse.peerevaluation.dto.PeerEvalSummaryDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/peer-evaluations")
@PreAuthorize("hasRole('STUDENT')")
public class PeerEvaluationController {

    private final PeerEvaluationService peerEvalService;

    public PeerEvaluationController(PeerEvaluationService peerEvalService) {
        this.peerEvalService = peerEvalService;
    }

    @GetMapping("/available-weeks")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableWeeks(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                peerEvalService.getAvailableWeeks(currentUserId(auth))));
    }

    // Returns just the week dates the student has already submitted.
    // Frontend uses this to label weeks in the dropdown as "Already Submitted".
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getMySubmittedWeeks(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                peerEvalService.getSubmittedWeeks(currentUserId(auth))));
    }

    // Returns team members, rubric criteria, and any existing evaluations for
    // the given week — everything the submission form needs in one call.
    @GetMapping("/context")
    public ResponseEntity<ApiResponse<PeerEvalContextResponse>> getContext(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                peerEvalService.getContext(currentUserId(auth), weekStartDate)));
    }

    // Student's own received-scores report for a week. Never exposes private
    // comments or evaluator identities — those are stripped in the service.
    @GetMapping("/my-report")
    public ResponseEntity<ApiResponse<PeerEvalReportResponse>> getMyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                peerEvalService.getMyReport(currentUserId(auth), weekStartDate)));
    }

    // Submit (or re-submit/edit) all peer evaluations for a week.
    @PostMapping
    public ResponseEntity<ApiResponse<List<PeerEvalSummaryDto>>> submitEvaluations(
            @Valid @RequestBody PeerEvalSubmitRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                peerEvalService.submitEvaluations(currentUserId(auth), request)));
    }

    private Long currentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}
