package com.projectpulse.team;

import com.projectpulse.section.SectionRepository;
import com.projectpulse.team.dto.AssignStudentsRequest;
import com.projectpulse.team.dto.TeamCreateRequest;
import com.projectpulse.team.dto.TeamDetailResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import com.projectpulse.team.dto.TeamUpdateRequest;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public TeamService(TeamRepository teamRepository,
                       SectionRepository sectionRepository,
                       UserRepository userRepository,
                       JavaMailSender mailSender) {
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public TeamDetailResponse createTeam(TeamCreateRequest request) {
        if (teamRepository.existsByName(request.name())) {
            throw new DuplicateTeamException("Team '" + request.name() + "' already exists");
        }

        var section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        TeamEntity team = new TeamEntity();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());
        team.setSection(section);

        TeamEntity saved = teamRepository.save(team);

        return toDetailResponse(saved);
    }

    public TeamDetailResponse getTeam(Long id) {
        TeamEntity team = teamRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        return toDetailResponse(team);
    }

    @Transactional
    public TeamDetailResponse updateTeam(Long id, TeamUpdateRequest request) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        if (teamRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateTeamException("Team '" + request.name() + "' already exists");
        }

        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());

        TeamEntity saved = teamRepository.save(team);
        return toDetailResponse(saved);
    }

    public List<TeamSummaryResponse> findTeams(Long sectionId, String name) {
        // TODO: add instructorId filter once Angel builds user/instructor assignment (UC-19)
        return teamRepository.findByFiltersWithStudents(sectionId, name).stream()
                .map(team -> new TeamSummaryResponse(
                        team.getId(),
                        team.getName(),
                        team.getDescription(),
                        team.getWebsiteUrl(),
                        team.getSection().getId(),
                        team.getSection().getName(),
                        toSummaryMemberDtos(team.getStudents()),
                        List.of() // instructors — populated when UC-19 is built
                ))
                .toList();
    }

    @Transactional
    public TeamDetailResponse assignStudents(Long teamId, AssignStudentsRequest request) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        List<UserEntity> students = userRepository.findAllById(request.studentIds());
        if (students.size() != request.studentIds().size()) {
            throw new NoSuchElementException("One or more students not found");
        }

        team.getStudents().addAll(
                students.stream()
                        .filter(s -> team.getStudents().stream().noneMatch(e -> e.getId().equals(s.getId())))
                        .toList()
        );

        TeamEntity saved = teamRepository.save(team);

        students.forEach(student -> sendAssignmentNotification(student, team));

        return toDetailResponse(saved);
    }

    @Transactional
    public TeamDetailResponse removeStudent(Long teamId, Long studentId) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        UserEntity student = team.getStudents().stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Student not found in team"));

        team.getStudents().remove(student);
        TeamEntity saved = teamRepository.save(team);

        sendRemovalNotification(student, team);

        return toDetailResponse(saved);
    }

    // ---- helpers ----

    private TeamDetailResponse toDetailResponse(TeamEntity team) {
        return new TeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getWebsiteUrl(),
                new TeamDetailResponse.SectionDto(
                        team.getSection().getId(),
                        team.getSection().getName()
                ),
                toMemberDtos(team.getStudents()),
                List.of() // instructors — populated when UC-19 is built
        );
    }

    private List<TeamDetailResponse.MemberDto> toMemberDtos(List<UserEntity> users) {
        return users.stream()
                .map(u -> new TeamDetailResponse.MemberDto(
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
    }

    private List<TeamSummaryResponse.MemberDto> toSummaryMemberDtos(List<UserEntity> users) {
        return users.stream()
                .map(u -> new TeamSummaryResponse.MemberDto(
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
    }

    private void sendRemovalNotification(UserEntity student, TeamEntity team) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(student.getEmail());
            helper.setSubject("You've been removed from a team — Peer Evaluation Tool");
            helper.setText(
                    "Hello " + student.getFirstName() + ",\n\n" +
                    "You have been removed from team \"" + team.getName() + "\".\n\n" +
                    "Please log in for more details: " + baseUrl + "\n\n" +
                    "Best regards,\nPeer Evaluation Tool Team",
                    false
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send removal notification to " + student.getEmail() + ": " + e.getMessage());
        }
    }

    private void sendAssignmentNotification(UserEntity student, TeamEntity team) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(student.getEmail());
            helper.setSubject("You've been assigned to a team — Peer Evaluation Tool");
            helper.setText(
                    "Hello " + student.getFirstName() + ",\n\n" +
                    "You have been assigned to team \"" + team.getName() + "\".\n\n" +
                    "Please log in to get started: " + baseUrl + "\n\n" +
                    "Best regards,\nPeer Evaluation Tool Team",
                    false
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send assignment notification to " + student.getEmail() + ": " + e.getMessage());
        }
    }
}
