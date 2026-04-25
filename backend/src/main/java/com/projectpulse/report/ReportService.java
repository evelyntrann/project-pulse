package com.projectpulse.report;

import com.projectpulse.peerevaluation.PeerEvaluationEntity;
import com.projectpulse.peerevaluation.PeerEvaluationRepository;
import com.projectpulse.peerevaluation.PeerEvaluationScoreEntity;
import com.projectpulse.report.dto.*;
import com.projectpulse.rubric.CriterionEntity;
import com.projectpulse.rubric.RubricRepository;
import com.projectpulse.section.ActiveWeekRepository;
import com.projectpulse.section.SectionEntity;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.team.TeamEntity;
import com.projectpulse.team.TeamRepository;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import com.projectpulse.war.WAREntity;
import com.projectpulse.war.WARRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SectionRepository sectionRepository;
    private final PeerEvaluationRepository peerEvalRepository;
    private final UserRepository userRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final RubricRepository rubricRepository;
    private final TeamRepository teamRepository;
    private final WARRepository warRepository;

    public ReportService(SectionRepository sectionRepository,
                         PeerEvaluationRepository peerEvalRepository,
                         UserRepository userRepository,
                         ActiveWeekRepository activeWeekRepository,
                         RubricRepository rubricRepository,
                         TeamRepository teamRepository,
                         WARRepository warRepository) {
        this.sectionRepository = sectionRepository;
        this.peerEvalRepository = peerEvalRepository;
        this.userRepository = userRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.rubricRepository = rubricRepository;
        this.teamRepository = teamRepository;
        this.warRepository = warRepository;
    }

    public List<InstructorSectionDto> getInstructorSections(Long instructorId) {
        return sectionRepository.findByEnrolledInstructorsId(instructorId)
                .stream()
                .map(s -> new InstructorSectionDto(s.getId(), s.getName()))
                .sorted(Comparator.comparing(InstructorSectionDto::name))
                .toList();
    }

    public List<LocalDate> getAvailableWeeksForSection(Long sectionId) {
        LocalDate today = LocalDate.now();
        return activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId)
                .stream()
                .filter(w -> w.isActive() && !w.getWeekStartDate().isAfter(today))
                .map(w -> w.getWeekStartDate())
                .toList();
    }

    // UC-31: section-wide peer eval report for one week.
    // Exposes evaluator names and private comments — INSTRUCTOR only.
    @Transactional(readOnly = true)
    public PeerEvalSectionReportResponse getPeerEvalSectionReport(Long sectionId, LocalDate weekStartDate) {
        SectionEntity sectionWithTeams = sectionRepository.findByIdWithTeams(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        SectionEntity sectionWithStudents = sectionRepository.findByIdWithEnrolledStudents(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        List<Long> teamIds = sectionWithTeams.getTeams().stream()
                .map(TeamEntity::getId)
                .toList();

        List<CriterionEntity> criteria = getCriterionEntities(sectionWithTeams);
        BigDecimal maxGrade = criteria.stream()
                .map(CriterionEntity::getMaxScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Load all evals for the section + week in one JOIN FETCH query.
        List<PeerEvaluationEntity> allEvals = teamIds.isEmpty() ? List.of()
                : peerEvalRepository.findByTeamIdInAndWeekStartDateWithScores(teamIds, weekStartDate);

        // Batch-load all users referenced in the evals (evaluators + evaluatees).
        Set<Long> referencedUserIds = new HashSet<>();
        allEvals.forEach(e -> {
            referencedUserIds.add(e.getEvaluatorId());
            referencedUserIds.add(e.getEvaluateeId());
        });
        Map<Long, UserEntity> userMap = userRepository.findAllById(referencedUserIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        Map<Long, List<PeerEvaluationEntity>> byEvaluatee = allEvals.stream()
                .collect(Collectors.groupingBy(PeerEvaluationEntity::getEvaluateeId));

        Set<Long> submitterIds = allEvals.stream()
                .map(PeerEvaluationEntity::getEvaluatorId)
                .collect(Collectors.toSet());

        List<UserEntity> enrolledStudents = sectionWithStudents.getEnrolledStudents();

        List<StudentPeerEvalEntryDto> studentEntries = enrolledStudents.stream()
                .sorted(Comparator.comparing(UserEntity::getLastName)
                        .thenComparing(UserEntity::getFirstName))
                .map(student -> buildStudentEntry(student, byEvaluatee, userMap, maxGrade, criteria))
                .toList();

        List<String> nonSubmitters = enrolledStudents.stream()
                .filter(s -> !submitterIds.contains(s.getId()))
                .sorted(Comparator.comparing(UserEntity::getLastName)
                        .thenComparing(UserEntity::getFirstName))
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();

        return new PeerEvalSectionReportResponse(
                weekStartDate, sectionWithTeams.getName(), maxGrade, studentEntries, nonSubmitters);
    }

    // UC-32: list of teams the instructor is assigned to (for team picker).
    public List<InstructorTeamDto> getInstructorTeams(Long instructorId) {
        return teamRepository.findByInstructorId(instructorId)
                .stream()
                .map(t -> new InstructorTeamDto(t.getId(), t.getName(), t.getSection().getName()))
                .sorted(Comparator.comparing(InstructorTeamDto::sectionName)
                        .thenComparing(InstructorTeamDto::teamName))
                .toList();
    }

    // UC-32: the team a student belongs to (for auto-selection on student view).
    @Transactional(readOnly = true)
    public Optional<InstructorTeamDto> getStudentTeam(Long studentId) {
        return userRepository.findTeamIdByStudentId(studentId)
                .flatMap(teamRepository::findById)
                .map(t -> new InstructorTeamDto(t.getId(), t.getName(), t.getSection().getName()));
    }

    // UC-32: available weeks for a team (delegates to section-level logic).
    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableWeeksForTeam(Long teamId) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        return getAvailableWeeksForSection(team.getSection().getId());
    }

    // UC-32: WAR report for all students on a team for one week.
    @Transactional(readOnly = true)
    public WARTeamReportResponse getWARTeamReport(Long teamId, LocalDate weekStartDate,
                                                   Long currentUserId, boolean isInstructor) {
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        if (isInstructor) {
            boolean assigned = team.getInstructors().stream()
                    .anyMatch(i -> i.getId().equals(currentUserId));
            if (!assigned) throw new IllegalArgumentException("You are not assigned to this team");
        } else {
            boolean member = team.getStudents().stream()
                    .anyMatch(s -> s.getId().equals(currentUserId));
            if (!member) throw new IllegalArgumentException("You are not a member of this team");
        }

        List<WAREntity> wars = warRepository.findByTeamIdAndWeekStartDateWithActivities(teamId, weekStartDate);
        Map<Long, WAREntity> warByStudentId = wars.stream()
                .collect(Collectors.toMap(w -> w.getStudent().getId(), w -> w));

        List<WARStudentEntryDto> studentEntries = team.getStudents().stream()
                .filter(s -> warByStudentId.containsKey(s.getId()))
                .sorted(Comparator.comparing(UserEntity::getLastName)
                        .thenComparing(UserEntity::getFirstName))
                .map(s -> {
                    WAREntity war = warByStudentId.get(s.getId());
                    List<WARActivityDto> activities = war.getActivities().stream()
                            .map(a -> new WARActivityDto(
                                    a.getCategory(),
                                    a.getPlannedActivity(),
                                    a.getDescription(),
                                    a.getPlannedHours(),
                                    a.getActualHours(),
                                    a.getStatus()))
                            .toList();
                    return new WARStudentEntryDto(s.getId(), s.getFirstName() + " " + s.getLastName(), activities);
                })
                .toList();

        List<String> nonSubmitters = team.getStudents().stream()
                .filter(s -> !warByStudentId.containsKey(s.getId()))
                .sorted(Comparator.comparing(UserEntity::getLastName)
                        .thenComparing(UserEntity::getFirstName))
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();

        return new WARTeamReportResponse(weekStartDate, team.getName(), studentEntries, nonSubmitters);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private StudentPeerEvalEntryDto buildStudentEntry(
            UserEntity student,
            Map<Long, List<PeerEvaluationEntity>> byEvaluatee,
            Map<Long, UserEntity> userMap,
            BigDecimal maxGrade,
            List<CriterionEntity> criteria) {

        List<PeerEvaluationEntity> received = byEvaluatee.getOrDefault(student.getId(), List.of());

        BigDecimal grade = BigDecimal.ZERO;
        if (!received.isEmpty()) {
            int sumOfTotals = received.stream()
                    .mapToInt(ev -> ev.getScores().stream()
                            .mapToInt(PeerEvaluationScoreEntity::getScore).sum())
                    .sum();
            grade = BigDecimal.valueOf(sumOfTotals)
                    .divide(BigDecimal.valueOf(received.size()), 2, RoundingMode.HALF_UP);
        }

        List<EvaluatorEntryDto> evaluations = received.stream()
                .map(ev -> {
                    UserEntity evaluator = userMap.get(ev.getEvaluatorId());
                    String evalName = evaluator != null
                            ? evaluator.getFirstName() + " " + evaluator.getLastName()
                            : "Unknown";
                    int total = ev.getScores().stream()
                            .mapToInt(PeerEvaluationScoreEntity::getScore).sum();
                    return new EvaluatorEntryDto(evalName, total,
                            ev.getPublicComment(), ev.getPrivateComment());
                })
                .sorted(Comparator.comparing(EvaluatorEntryDto::evaluatorName))
                .toList();

        String fullName = student.getFirstName() + " " + student.getLastName();
        return new StudentPeerEvalEntryDto(student.getId(), fullName, grade, evaluations);
    }

    private List<CriterionEntity> getCriterionEntities(SectionEntity section) {
        if (section.getRubricId() == null) return List.of();
        return rubricRepository.findById(section.getRubricId())
                .map(r -> r.getCriteria())
                .orElse(List.of());
    }
}
