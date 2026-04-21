package com.projectpulse.team;

import com.projectpulse.section.SectionEntity;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.team.dto.AssignStudentsRequest;
import com.projectpulse.team.dto.TeamCreateRequest;
import com.projectpulse.team.dto.TeamDetailResponse;
import com.projectpulse.team.dto.TeamSummaryResponse;
import com.projectpulse.team.dto.TeamTransferResponse;
import com.projectpulse.team.dto.TeamUpdateRequest;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    public TeamService(TeamRepository teamRepository,
                       SectionRepository sectionRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
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
                        team.getInstructor() != null
                                ? List.of(new TeamSummaryResponse.MemberDto(
                                        team.getInstructor().getId(),
                                        team.getInstructor().getFirstName(),
                                        team.getInstructor().getLastName(),
                                        team.getInstructor().getEmail()))
                                : List.of()
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

        for (UserEntity student : students) {
            // Skip if already on this team
            if (team.getStudents().stream().anyMatch(s -> s.getId().equals(student.getId()))) continue;
            // Remove from any other team in the same section first
            teamRepository.findBySectionAndStudent(team.getSection().getId(), student)
                    .ifPresent(oldTeam -> oldTeam.getStudents().remove(student));
            team.getStudents().add(student);
        }

        return toDetailResponse(teamRepository.save(team));
    }

    @Transactional
    public void assignInstructorToTeam(Long teamId, Long instructorId) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        UserEntity instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

        if (!"INSTRUCTOR".equals(instructor.getRole()) && !"ADMIN".equals(instructor.getRole())) {
            throw new IllegalArgumentException("User is not an instructor");
        }

        // Replace existing instructor if different
        if (!instructor.equals(team.getInstructor())) {
            team.setInstructor(instructor);
            teamRepository.save(team);
        }
    }

    @Transactional
    public TeamTransferResponse transferTeamToAnotherSection(Long teamId, Long newSectionId) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        SectionEntity oldSection = team.getSection();
        SectionEntity newSection = sectionRepository.findById(newSectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        if (oldSection.getId().equals(newSection.getId())) {
            throw new IllegalArgumentException("Team is already in the specified section");
        }

        int studentCount = team.getStudents().size();
        team.setSection(newSection);
        teamRepository.save(team);

        return new TeamTransferResponse(
                team.getId(),
                team.getName(),
                oldSection.getId(),
                oldSection.getName(),
                newSection.getId(),
                newSection.getName(),
                studentCount
        );
    }

    @Transactional
    public void removeInstructorFromTeam(Long teamId) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        team.setInstructor(null);
        teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        teamRepository.delete(team);

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

        return toDetailResponse(saved);
    }

    // ---- helpers ----

    private TeamDetailResponse toDetailResponse(TeamEntity team) {
        List<TeamDetailResponse.MemberDto> instructors = team.getInstructor() != null
                ? List.of(new TeamDetailResponse.MemberDto(
                        team.getInstructor().getId(),
                        team.getInstructor().getFirstName(),
                        team.getInstructor().getLastName(),
                        team.getInstructor().getEmail()))
                : List.of();

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
                instructors
        );
    }

    private List<TeamDetailResponse.MemberDto> toMemberDtos(java.util.Collection<UserEntity> users) {
        return users.stream()
                .map(u -> new TeamDetailResponse.MemberDto(
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
    }

    private List<TeamSummaryResponse.MemberDto> toSummaryMemberDtos(java.util.Collection<UserEntity> users) {
        return users.stream()
                .map(u -> new TeamSummaryResponse.MemberDto(
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
    }

}
