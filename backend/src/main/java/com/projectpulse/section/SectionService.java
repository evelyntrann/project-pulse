package com.projectpulse.section;

import com.projectpulse.invitation.InvitationEntity;
import com.projectpulse.invitation.InvitationRepository;
import com.projectpulse.section.dto.ActiveWeekResponse;
import com.projectpulse.section.dto.ActiveWeeksRequest;
import com.projectpulse.section.dto.SectionCreateRequest;
import com.projectpulse.section.dto.SectionDetailResponse;
import com.projectpulse.section.dto.SectionStudentResponse;
import com.projectpulse.section.dto.SectionSummaryResponse;
import com.projectpulse.section.dto.SectionUpdateRequest;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public SectionService(SectionRepository sectionRepository,
                          ActiveWeekRepository activeWeekRepository,
                          UserRepository userRepository,
                          InvitationRepository invitationRepository) {
        this.sectionRepository = sectionRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
    }

    public List<SectionSummaryResponse> findSections(String name) {
        return sectionRepository.findByNameContaining(name).stream()
                .map(section -> new SectionSummaryResponse(
                        section.getId(),
                        section.getName(),
                        section.getStartDate(),
                        section.getEndDate(),
                        section.getTeams().stream()
                                .map(t -> t.getName())
                                .sorted()
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public SectionDetailResponse createSection(SectionCreateRequest request) {
        if (sectionRepository.existsByName(request.name())) {
            throw new DuplicateSectionException("Section '" + request.name() + "' already exists");
        }

        SectionEntity section = new SectionEntity();
        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubricId(request.rubricId());
        if (request.isActive() != null) section.setActive(request.isActive());
        section.setWarWeeklyDueDay(request.warWeeklyDueDay());
        section.setWarDueTime(request.warDueTime());
        section.setPeerEvaluationWeeklyDueDay(request.peerEvaluationWeeklyDueDay());
        section.setPeerEvaluationDueTime(request.peerEvaluationDueTime());

        SectionEntity saved = sectionRepository.save(section);

        return toDetailResponse(saved, List.of(), List.of(), List.of());
    }

    @Transactional
    public SectionDetailResponse updateSection(Long id, SectionUpdateRequest request) {
        SectionEntity section = sectionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        if (!section.getName().equals(request.name()) && sectionRepository.existsByName(request.name())) {
            throw new DuplicateSectionException("Section '" + request.name() + "' already exists");
        }

        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubricId(request.rubricId());
        if (request.isActive() != null) section.setActive(request.isActive());
        section.setWarWeeklyDueDay(request.warWeeklyDueDay());
        section.setWarDueTime(request.warDueTime());
        section.setPeerEvaluationWeeklyDueDay(request.peerEvaluationWeeklyDueDay());
        section.setPeerEvaluationDueTime(request.peerEvaluationDueTime());

        SectionEntity saved = sectionRepository.save(section);

        return toDetailResponse(saved, List.of(), List.of(), List.of());
    }

    @Transactional
    public void deleteSection(Long id) {
        sectionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        sectionRepository.deleteById(id);
    }

    public List<ActiveWeekResponse> getActiveWeeks(Long sectionId) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        return activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId).stream()
                .map(w -> new ActiveWeekResponse(w.getWeekStartDate(), w.isActive()))
                .toList();
    }

    @Transactional
    public List<ActiveWeekResponse> setActiveWeeks(Long sectionId, ActiveWeeksRequest request) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        activeWeekRepository.deleteBySectionId(sectionId);

        List<ActiveWeekEntity> entities = request.weeks().stream()
                .map(w -> {
                    ActiveWeekEntity entity = new ActiveWeekEntity();
                    entity.setSectionId(sectionId);
                    entity.setWeekStartDate(w.weekStartDate());
                    entity.setActive(w.isActive());
                    return entity;
                })
                .toList();

        activeWeekRepository.saveAll(entities);

        return entities.stream()
                .map(w -> new ActiveWeekResponse(w.getWeekStartDate(), w.isActive()))
                .toList();
    }

    public SectionDetailResponse getSection(Long id) {
        SectionEntity section = sectionRepository.findByIdWithTeamsAndStudents(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        SectionEntity sectionWithInstructors = sectionRepository.findByIdWithInstructors(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        // Collect all student IDs already on a team in this section
        var assignedStudentIds = section.getTeams().stream()
                .flatMap(t -> t.getStudents().stream())
                .map(s -> s.getId())
                .collect(Collectors.toSet());

        // Collect all instructor IDs already assigned to a team in this section
        Set<Long> assignedInstructorIds = section.getTeams().stream()
                .filter(t -> t.getInstructor() != null)
                .map(t -> t.getInstructor().getId())
                .collect(Collectors.toSet());

        List<SectionDetailResponse.TeamDto> teamDtos = section.getTeams().stream()
                .map(team -> {
                    List<SectionDetailResponse.UserDto> teamInstructors;
                    if (team.getInstructor() != null) {
                        UserEntity instr = team.getInstructor();
                        teamInstructors = List.of(new SectionDetailResponse.UserDto(
                                instr.getId(), instr.getFirstName(), instr.getLastName(), instr.getEmail()));
                    } else {
                        teamInstructors = List.of();
                    }
                    return new SectionDetailResponse.TeamDto(
                            team.getId(),
                            team.getName(),
                            team.getDescription(),
                            team.getWebsiteUrl(),
                            team.getStudents().stream()
                                    .map(s -> new SectionDetailResponse.UserDto(
                                            s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                                    .toList(),
                            teamInstructors
                    );
                })
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();

        List<SectionDetailResponse.UserDto> unassignedStudents = section.getEnrolledStudents().stream()
                .filter(s -> !assignedStudentIds.contains(s.getId()))
                .map(s -> new SectionDetailResponse.UserDto(
                        s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                .toList();

        List<SectionDetailResponse.UserDto> unassignedInstructors = sectionWithInstructors.getEnrolledInstructors().stream()
                .filter(i -> !assignedInstructorIds.contains(i.getId()))
                .map(i -> new SectionDetailResponse.UserDto(
                        i.getId(), i.getFirstName(), i.getLastName(), i.getEmail()))
                .toList();

        return toDetailResponse(section, teamDtos, unassignedStudents, unassignedInstructors);
    }

    public List<SectionStudentResponse> getEnrolledStudents(Long sectionId) {
        SectionEntity section = sectionRepository.findByIdWithEnrolledStudents(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        return section.getEnrolledStudents().stream()
                .map(s -> new SectionStudentResponse(
                        s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                .toList();
    }

    @Transactional
    public void assignRubric(Long sectionId, Long rubricId) {
        SectionEntity section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        section.setRubricId(rubricId);
        sectionRepository.save(section);
    }

    @Transactional
    public void assignInstructor(Long sectionId, Long instructorId) {
        SectionEntity section = sectionRepository.findByIdWithInstructors(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        UserEntity instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found"));
        if (!"INSTRUCTOR".equals(instructor.getRole()) && !"ADMIN".equals(instructor.getRole())) {
            throw new IllegalArgumentException("User is not an instructor");
        }
        section.getEnrolledInstructors().add(instructor);
        sectionRepository.save(section);
    }

    public List<SectionDetailResponse.UserDto> getInstructors(Long sectionId) {
        SectionEntity section = sectionRepository.findByIdWithInstructors(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        return section.getEnrolledInstructors().stream()
                .map(i -> new SectionDetailResponse.UserDto(
                        i.getId(), i.getFirstName(), i.getLastName(), i.getEmail()))
                .toList();
    }

    @Transactional
    public void removeInstructor(Long sectionId, Long instructorId) {
        SectionEntity section = sectionRepository.findByIdWithInstructors(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        UserEntity instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

        if (!section.getEnrolledInstructors().contains(instructor)) {
            throw new NoSuchElementException("Instructor is not assigned to this section");
        }
        if (section.getEnrolledInstructors().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last instructor from the section");
        }
        section.getEnrolledInstructors().remove(instructor);
        sectionRepository.save(section);
    }

    @Transactional
    public Map<String, Object> inviteOrAddInstructors(Long sectionId, List<String> emails) {
        SectionEntity section = sectionRepository.findByIdWithInstructors(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        List<String> addedEmails = new ArrayList<>();
        List<String> invitedEmails = new ArrayList<>();
        List<String> alreadyExistsEmails = new ArrayList<>();

        for (String email : emails) {
            UserEntity existingUser = userRepository.findByEmail(email).orElse(null);

            if (existingUser != null) {
                if (!"INSTRUCTOR".equals(existingUser.getRole()) && !"ADMIN".equals(existingUser.getRole())) {
                    throw new IllegalArgumentException("User with email " + email + " exists but is not an instructor");
                }
                if (section.getEnrolledInstructors().contains(existingUser)) {
                    alreadyExistsEmails.add(email);
                } else {
                    section.getEnrolledInstructors().add(existingUser);
                    addedEmails.add(email);
                }
            } else {
                createInstructorInvitation(email, sectionId);
                invitedEmails.add(email);
            }
        }

        sectionRepository.save(section);

        Map<String, Object> result = new HashMap<>();
        result.put("added", addedEmails);
        result.put("invited", invitedEmails);
        result.put("alreadyExists", alreadyExistsEmails);
        return result;
    }

    // ---- helpers ----

    private void createInstructorInvitation(String email, Long sectionId) {
        SectionEntity section = sectionRepository.findById(sectionId).orElse(null);

        InvitationEntity invite = new InvitationEntity();
        invite.setEmail(email);
        invite.setRole("INSTRUCTOR");
        invite.setSection(section);
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invite);
    }

    private SectionDetailResponse toDetailResponse(SectionEntity section,
                                                    List<SectionDetailResponse.TeamDto> teams,
                                                    List<SectionDetailResponse.UserDto> unassignedStudents,
                                                    List<SectionDetailResponse.UserDto> unassignedInstructors) {
        return new SectionDetailResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                section.getRubricId(),
                section.isActive(),
                section.getWarWeeklyDueDay(),
                section.getWarDueTime(),
                section.getPeerEvaluationWeeklyDueDay(),
                section.getPeerEvaluationDueTime(),
                teams,
                unassignedStudents,
                unassignedInstructors
        );
    }
}
