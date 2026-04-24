package com.projectpulse.user;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.user.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        Long currentUserId = Long.parseLong(authentication.getName());
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only edit your own account."));
        }

        userService.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
