package com.projectpulse.registration;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.registration.dto.InvitationValidateResponse;
import com.projectpulse.registration.dto.RegisterResponse;
import com.projectpulse.registration.dto.StudentRegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * UC-25 Step 2 support — called on page load to validate the token
     * before showing the registration form.
     * Public endpoint (no JWT required — student has no account yet).
     */
    @GetMapping("/api/v1/invitations/validate")
    public ResponseEntity<ApiResponse<InvitationValidateResponse>> validateToken(
            @RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(registrationService.validateToken(token)));
    }

    /**
     * UC-25 Steps 3–7 — student submits the registration form.
     * Returns 201 Created on success (POST-1).
     * Public endpoint — student has no account yet.
     */
    @PostMapping("/api/v1/students/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerStudent(
            @Valid @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(registrationService.registerStudent(request)));
    }
}
