package com.projectpulse.section;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSection(id)));
    }
}
