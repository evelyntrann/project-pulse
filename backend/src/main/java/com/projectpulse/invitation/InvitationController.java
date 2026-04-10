package com.projectpulse.invitation;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.invitation.dto.StudentInviteRequest;
import com.projectpulse.invitation.dto.StudentInviteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentInviteResponse>> inviteStudents(
            @Valid @RequestBody StudentInviteRequest request,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(invitationService.inviteStudents(request, adminId)));
    }
}
