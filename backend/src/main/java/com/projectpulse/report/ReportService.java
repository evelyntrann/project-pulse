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
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
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

    public ReportService(SectionRepository sectionRepository,
                         PeerEvaluationRepository peerEvalRepository,
                         UserRepository userRepository,
                         ActiveWeekRepository activeWeekRepository,
                         RubricRepository rubricRepository) {
        this.sectionRepository = sectionRepository;
        this.peerEvalRepository = peerEvalRepository;
        this.userRepository = userRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.rubricRepository = rubricRepository;
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
