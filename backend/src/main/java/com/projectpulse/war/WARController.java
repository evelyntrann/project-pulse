package com.projectpulse.war;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.war.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wars")
@PreAuthorize("hasRole('STUDENT')")
public class WARController {

    private final WARService warService;

    public WARController(WARService warService) {
        this.warService = warService;
    }

    @GetMapping("/available-weeks")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableWeeks(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(warService.getAvailableWeeks(currentUserId(authentication))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WARSummaryResponse>>> getWARs(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(warService.getWARs(currentUserId(authentication))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WARDetailResponse>> getWAR(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(warService.getWAR(currentUserId(authentication), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WARDetailResponse>> createWAR(
            @Valid @RequestBody WARCreateRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(warService.createWAR(currentUserId(authentication), request.weekStartDate())));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<WARDetailResponse>> submitWAR(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(warService.submitWAR(currentUserId(authentication), id)));
    }

    @PostMapping("/{warId}/activities")
    public ResponseEntity<ApiResponse<WARDetailResponse>> addActivity(
            @PathVariable Long warId,
            @Valid @RequestBody ActivityRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(warService.addActivity(currentUserId(authentication), warId, request)));
    }

    @PutMapping("/{warId}/activities/{activityId}")
    public ResponseEntity<ApiResponse<WARDetailResponse>> updateActivity(
            @PathVariable Long warId,
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                warService.updateActivity(currentUserId(authentication), warId, activityId, request)));
    }

    @DeleteMapping("/{warId}/activities/{activityId}")
    public ResponseEntity<ApiResponse<WARDetailResponse>> deleteActivity(
            @PathVariable Long warId,
            @PathVariable Long activityId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                warService.deleteActivity(currentUserId(authentication), warId, activityId)));
    }

    private Long currentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
