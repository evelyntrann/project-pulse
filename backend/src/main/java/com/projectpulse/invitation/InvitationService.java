package com.projectpulse.invitation;

import com.projectpulse.invitation.dto.InviteLinkRequest;
import com.projectpulse.invitation.dto.InviteLinkResponse;
import com.projectpulse.invitation.dto.InvitationInfoResponse;
import com.projectpulse.invitation.dto.StudentRegisterRequest;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public InvitationService(InvitationRepository invitationRepository,
                             SectionRepository sectionRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.invitationRepository = invitationRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InviteLinkResponse generateInviteLink(InviteLinkRequest request, Long adminId) {
        var section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        var admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        InvitationEntity invite = new InvitationEntity();
        invite.setRole("STUDENT");
        invite.setSection(section);
        invite.setToken(token);
        invite.setInvitedBy(admin);
        invite.setExpiresAt(expiresAt);
        invitationRepository.save(invite);

        return new InviteLinkResponse(baseUrl + "/join/" + token, expiresAt);
    }

    public InvitationInfoResponse getInvitationInfo(String token) {
        InvitationEntity invite = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

        if (invite.getAcceptedAt() != null) {
            throw new IllegalStateException("This invitation link has already been used.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This invitation link has expired.");
        }

        return new InvitationInfoResponse(
                invite.getSection().getName(),
                invite.getExpiresAt()
        );
    }

    @Transactional
    public void registerViaToken(String token, StudentRegisterRequest request) {
        InvitationEntity invite = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

        if (invite.getAcceptedAt() != null) {
            throw new IllegalStateException("This invitation link has already been used.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This invitation link has expired.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        UserEntity student = new UserEntity();
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setPasswordHash(passwordEncoder.encode(request.password()));
        student.setRole("STUDENT");
        userRepository.save(student);

        var section = invite.getSection();
        section.getEnrolledStudents().add(student);
        sectionRepository.save(section);

        invite.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invite);
    }
}