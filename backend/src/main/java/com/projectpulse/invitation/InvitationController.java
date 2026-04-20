package com.projectpulse.invitation;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.invitation.dto.InviteLinkRequest;
import com.projectpulse.invitation.dto.InviteLinkResponse;
import com.projectpulse.invitation.dto.InvitationInfoResponse;
import com.projectpulse.invitation.dto.StudentRegisterRequest;
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

    @PostMapping("/students/link")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InviteLinkResponse>> generateInviteLink(
            @Valid @RequestBody InviteLinkRequest request,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(invitationService.generateInviteLink(request, adminId)));
    }

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<InvitationInfoResponse>> getInvitationInfo(
            @PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.getInvitationInfo(token)));
    }

    @PostMapping("/{token}/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @PathVariable String token,
            @Valid @RequestBody StudentRegisterRequest request) {
        invitationService.registerViaToken(token, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
