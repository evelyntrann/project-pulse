package com.projectpulse.section;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.section.dto.ActiveWeekResponse;
import com.projectpulse.section.dto.ActiveWeeksRequest;
import com.projectpulse.section.dto.SectionCreateRequest;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import com.projectpulse.section.dto.SectionUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<SectionDetailResponse>> createSection(@Valid @RequestBody SectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(sectionService.createSection(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.updateSection(id, request)));
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSection(id)));
    }
}
