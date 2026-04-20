package com.projectpulse.registration;

import com.projectpulse.invitation.InvitationEntity;
import com.projectpulse.invitation.InvitationRepository;
import com.projectpulse.registration.dto.InvitationValidateResponse;
import com.projectpulse.registration.dto.RegisterResponse;
import com.projectpulse.registration.dto.StudentRegisterRequest;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class RegistrationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(InvitationRepository invitationRepository,
                               UserRepository userRepository,
                               SectionRepository sectionRepository,
                               PasswordEncoder passwordEncoder) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * UC-25 Step 2 + Extension 2a
     * Called when the registration page loads. Lets the frontend know whether
     * to show the form, a "link expired" message, or redirect to login (2a).
     * @Transactional(readOnly) needed because InvitationEntity.section is LAZY.
     */
    @Transactional(readOnly = true)
    public InvitationValidateResponse validateToken(String token) {
        InvitationEntity invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invalid registration link"));

        boolean expired = invitation.getExpiresAt().isBefore(LocalDateTime.now());
        boolean alreadyUsed = invitation.getAcceptedAt() != null;
        String sectionName = (invitation.getSection() != null) ? invitation.getSection().getName() : null;

        return new InvitationValidateResponse(
                invitation.getEmail(),
                invitation.getRole(),
                sectionName,
                expired,
                alreadyUsed
        );
    }

    /**
     * UC-25 Steps 4, 7, POST-1
     * Validates inputs, creates the student account, enrolls in section,
     * and marks the invitation as accepted.
     */
    @Transactional
    public RegisterResponse registerStudent(StudentRegisterRequest request) {

        // Step 4 — validate the token is real and for a student
        InvitationEntity invitation = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new NoSuchElementException("Invalid registration link"));

        if (!"STUDENT".equals(invitation.getRole())) {
            throw new IllegalArgumentException("This invitation link is not for a student account");
        }

        // Step 4 — validate the invitation is still usable
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This registration link has expired");
        }

        // Extension 2a — student already registered using this link
        if (invitation.getAcceptedAt() != null) {
            throw new IllegalStateException("This invitation has already been used. Please log in.");
        }

        // Extension 2a — account with this email somehow already exists
        if (userRepository.findByEmail(invitation.getEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists. Please log in.");
        }

        // Step 7, POST-1 — create the student account
        UserEntity user = new UserEntity();
        user.setEmail(invitation.getEmail());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("STUDENT");
        user.setActive(true);
        UserEntity saved = userRepository.save(user);

        // POST-1 — enroll the student in the section they were invited to
        // (business rule: student must be in a section before being assigned to a team)
        if (invitation.getSection() != null) {
            var section = sectionRepository
                    .findByIdWithEnrolledStudents(invitation.getSection().getId())
                    .orElseThrow(() -> new NoSuchElementException("Section not found"));
            section.getEnrolledStudents().add(saved);
            sectionRepository.save(section);
        }

        // Mark the invitation as consumed so it can't be reused (Extension 2a prevention)
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        return new RegisterResponse(
                saved.getId(), saved.getEmail(),
                saved.getFirstName(), saved.getLastName(), saved.getRole());
    }
}
