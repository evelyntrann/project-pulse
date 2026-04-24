package com.projectpulse.user;

import com.projectpulse.invitation.InvitationEntity;
import com.projectpulse.invitation.InvitationRepository;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.user.dto.StudentDetailResponse;
import com.projectpulse.user.dto.StudentRegisterRequest;
import com.projectpulse.user.dto.StudentSearchResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(UserRepository userRepository,
                          InvitationRepository invitationRepository,
                          SectionRepository sectionRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.sectionRepository = sectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentSearchResponse> searchStudents(
            String firstName, String lastName, String email,
            String sectionName, String teamName,
            Long sectionId, Long teamId) {

        // Convert blank strings to null so the IS NULL check in the query works correctly
        return userRepository.searchStudents(
                        blank(firstName), blank(lastName), blank(email),
                        blank(sectionName), blank(teamName),
                        sectionId, teamId)
                .stream()
                .map(p -> new StudentSearchResponse(
                        p.getId(), p.getFirstName(), p.getLastName(),
                        p.getEmail(), p.getSectionName(), p.getTeamName()))
                .toList();
    }

    public StudentDetailResponse getStudent(Long id) {
        StudentSearchProjection p = userRepository.findStudentById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));
        return new StudentDetailResponse(
                p.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getSectionName(), p.getTeamName());
    }

    @Transactional
    public void registerStudent(StudentRegisterRequest request) {
        InvitationEntity invite = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));

        if (invite.getAcceptedAt() != null) {
            throw new IllegalStateException("This invitation link has already been used.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This invitation link has expired.");
        }
        if (!"STUDENT".equals(invite.getRole())) {
            throw new IllegalArgumentException("This invitation link is not valid for student registration.");
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

        if (invite.getSection() != null) {
            var section = invite.getSection();
            section.getEnrolledStudents().add(student);
            sectionRepository.save(section);
        }

        invite.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invite);
    }

    @Transactional
    public void deleteStudent(Long id) {
        userRepository.findStudentById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));
        userRepository.deleteSectionMemberships(id);
        userRepository.deleteTeamMemberships(id);
        userRepository.deleteById(id);
    }

    private String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
