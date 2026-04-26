package com.projectpulse.peerevaluation;

import com.projectpulse.peerevaluation.dto.*;

import java.math.RoundingMode;
import com.projectpulse.rubric.CriterionEntity;
import com.projectpulse.rubric.RubricEntity;
import com.projectpulse.rubric.RubricRepository;
import com.projectpulse.section.ActiveWeekRepository;
import com.projectpulse.section.SectionEntity;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.team.TeamEntity;
import com.projectpulse.team.TeamRepository;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PeerEvaluationService {

    private final PeerEvaluationRepository peerEvalRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;

    public PeerEvaluationService(PeerEvaluationRepository peerEvalRepository,
                                 UserRepository userRepository,
                                 TeamRepository teamRepository,
                                 ActiveWeekRepository activeWeekRepository,
                                 SectionRepository sectionRepository,
                                 RubricRepository rubricRepository) {
        this.peerEvalRepository = peerEvalRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
    }

    // Same logic as WARService: active weeks for this student's section, up to today.
    public List<LocalDate> getAvailableWeeks(Long studentId) {
        Long sectionId = userRepository.findSectionIdByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("You are not enrolled in a section."));
        LocalDate today = LocalDate.now();
        return activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId)
                .stream()
                .filter(w -> w.isActive() && !w.getWeekStartDate().isAfter(today))
                .map(w -> w.getWeekStartDate())
                .toList();
    }

    // Returns just the week dates where the student has already submitted at least one evaluation.
    // Used by the frontend week dropdown to show "Already Submitted" labels.
    public List<LocalDate> getSubmittedWeeks(Long evaluatorId) {
        return peerEvalRepository.findSubmittedWeeksByEvaluatorId(evaluatorId);
    }

    // Single endpoint that gives the form everything it needs:
    // who to evaluate, what criteria to score, and existing evaluations if editing.
    @Transactional(readOnly = true)
    public PeerEvalContextResponse getContext(Long studentId, LocalDate weekStartDate) {
        Long sectionId = userRepository.findSectionIdByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("You are not enrolled in a section."));
        Long teamId = userRepository.findTeamIdByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("You are not assigned to a team."));
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        List<TeamMemberDto> members = team.getStudents().stream()
                .map(u -> new TeamMemberDto(u.getId(), u.getFirstName(), u.getLastName()))
                .sorted(Comparator.comparing(TeamMemberDto::lastName)
                        .thenComparing(TeamMemberDto::firstName))
                .toList();

        List<CriterionDto> criteria = getCriteriaForSection(sectionId);

        List<PeerEvalSummaryDto> existing = peerEvalRepository
                .findByEvaluatorIdAndWeekStartDate(studentId, weekStartDate)
                .stream()
                .map(this::toSummaryDto)
                .toList();

        return new PeerEvalContextResponse(members, criteria, existing);
    }

    @Transactional
    public List<PeerEvalSummaryDto> submitEvaluations(Long evaluatorId, PeerEvalSubmitRequest request) {
        Long sectionId = userRepository.findSectionIdByStudentId(evaluatorId)
                .orElseThrow(() -> new IllegalStateException("You are not enrolled in a section."));
        Long teamId = userRepository.findTeamIdByStudentId(evaluatorId)
                .orElseThrow(() -> new IllegalStateException("You are not assigned to a team."));
        TeamEntity team = teamRepository.findByIdWithStudents(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        // Validate the selected week is currently active for this section.
        boolean weekValid = activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId)
                .stream()
                .anyMatch(w -> w.isActive() && w.getWeekStartDate().equals(request.weekStartDate()));
        if (!weekValid) {
            throw new IllegalArgumentException("The selected week is not active for your section.");
        }

        // Every team member must appear exactly once — no missing evaluations, no extras.
        Set<Long> teamMemberIds = team.getStudents().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
        Set<Long> submittedIds = request.evaluations().stream()
                .map(PeerEvalSubmitRequest.EvaluationEntry::evaluateeId)
                .collect(Collectors.toSet());
        if (!teamMemberIds.equals(submittedIds)) {
            throw new IllegalArgumentException(
                    "You must submit an evaluation for every team member, including yourself.");
        }

        // Validate scores against the section's rubric criteria.
        List<CriterionEntity> criteria = getCriterionEntities(sectionId);
        Set<Long> criterionIds = criteria.stream()
                .map(CriterionEntity::getId)
                .collect(Collectors.toSet());
        Map<Long, BigDecimal> maxScoreMap = criteria.stream()
                .collect(Collectors.toMap(CriterionEntity::getId, CriterionEntity::getMaxScore));

        for (PeerEvalSubmitRequest.EvaluationEntry entry : request.evaluations()) {
            Set<Long> submittedCriteria = entry.scores().stream()
                    .map(PeerEvalSubmitRequest.ScoreEntry::criterionId)
                    .collect(Collectors.toSet());
            if (!submittedCriteria.equals(criterionIds)) {
                throw new IllegalArgumentException(
                        "Each evaluation must include a score for every rubric criterion.");
            }
            for (PeerEvalSubmitRequest.ScoreEntry se : entry.scores()) {
                BigDecimal max = maxScoreMap.get(se.criterionId());
                if (se.score() > max.intValue()) {
                    throw new IllegalArgumentException(
                            "Score " + se.score() + " exceeds the maximum of " + max.intValue()
                            + " for this criterion.");
                }
            }
        }

        // Upsert: find existing row or create new, replace scores, update timestamps.
        List<PeerEvalSummaryDto> results = new ArrayList<>();
        for (PeerEvalSubmitRequest.EvaluationEntry entry : request.evaluations()) {
            PeerEvaluationEntity eval = peerEvalRepository
                    .findByEvaluatorIdAndEvaluateeIdAndWeekStartDate(
                            evaluatorId, entry.evaluateeId(), request.weekStartDate())
                    .orElseGet(() -> {
                        PeerEvaluationEntity e = new PeerEvaluationEntity();
                        e.setEvaluatorId(evaluatorId);
                        e.setEvaluateeId(entry.evaluateeId());
                        e.setTeamId(teamId);
                        e.setWeekStartDate(request.weekStartDate());
                        return e;
                    });

            eval.setPublicComment(entry.publicComment());
            eval.setPrivateComment(entry.privateComment());
            eval.setSubmittedAt(LocalDateTime.now());

            // saveAndFlush after clear() forces Hibernate to execute the DELETE
            // statements for orphaned scores immediately, before the INSERT loop
            // below. Without this, Hibernate batches inserts before orphan-removal
            // deletes, which violates the (peer_evaluation_id, criterion_id) unique
            // constraint on re-submission.
            eval.getScores().clear();
            peerEvalRepository.saveAndFlush(eval);

            for (PeerEvalSubmitRequest.ScoreEntry se : entry.scores()) {
                PeerEvaluationScoreEntity score = new PeerEvaluationScoreEntity();
                score.setPeerEvaluation(eval);
                score.setCriterionId(se.criterionId());
                score.setScore(se.score());
                eval.getScores().add(score);
            }
            results.add(toSummaryDto(eval));
        }
        return results;
    }

    // UC-29: the student's own received-scores report for one week.
    // Never exposes private comments or evaluator identities.
    @Transactional(readOnly = true)
    public PeerEvalReportResponse getMyReport(Long studentId, LocalDate weekStartDate) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        // JOIN FETCH loads scores in the same query — no N+1.
        List<PeerEvaluationEntity> evals = peerEvalRepository
                .findByEvaluateeIdAndWeekStartDateWithScores(studentId, weekStartDate);

        String name = student.getFirstName() + " " + student.getLastName();

        if (evals.isEmpty()) {
            return new PeerEvalReportResponse(
                    weekStartDate, name, 0, List.of(), List.of(),
                    BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Long sectionId = userRepository.findSectionIdByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("You are not enrolled in a section."));
        List<CriterionEntity> criteria = getCriterionEntities(sectionId);

        int n = evals.size();

        // For each criterion: average all received scores, accumulate grade totals.
        List<CriterionAverageDto> criterionAverages = new ArrayList<>();
        BigDecimal gradeNumerator   = BigDecimal.ZERO;
        BigDecimal gradeDenominator = BigDecimal.ZERO;

        for (CriterionEntity criterion : criteria) {
            List<Integer> scores = evals.stream()
                    .flatMap(ev -> ev.getScores().stream())
                    .filter(s -> s.getCriterionId().equals(criterion.getId()))
                    .map(PeerEvaluationScoreEntity::getScore)
                    .toList();

            BigDecimal avg = BigDecimal.ZERO;
            if (!scores.isEmpty()) {
                int total = scores.stream().mapToInt(Integer::intValue).sum();
                avg = BigDecimal.valueOf(total)
                        .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                gradeNumerator = gradeNumerator.add(BigDecimal.valueOf(total));
            }

            // Denominator: each evaluator could give up to maxScore for this criterion.
            gradeDenominator = gradeDenominator.add(
                    criterion.getMaxScore().multiply(BigDecimal.valueOf(n)));

            criterionAverages.add(new CriterionAverageDto(
                    criterion.getId(), criterion.getName(), avg, criterion.getMaxScore()));
        }

        // Public comments — exclude null/blank; evaluator identity is never included.
        List<String> publicComments = evals.stream()
                .map(PeerEvaluationEntity::getPublicComment)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        return new PeerEvalReportResponse(
                weekStartDate, name, n,
                criterionAverages, publicComments,
                gradeNumerator, gradeDenominator);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<CriterionDto> getCriteriaForSection(Long sectionId) {
        return getCriterionEntities(sectionId).stream()
                .map(c -> new CriterionDto(c.getId(), c.getName(), c.getDescription(), c.getMaxScore()))
                .toList();
    }

    private List<CriterionEntity> getCriterionEntities(Long sectionId) {
        SectionEntity section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));
        if (section.getRubricId() == null) return List.of();
        return rubricRepository.findById(section.getRubricId())
                .map(RubricEntity::getCriteria)
                .orElse(List.of());
    }

    private PeerEvalSummaryDto toSummaryDto(PeerEvaluationEntity eval) {
        List<ScoreSummaryDto> scores = eval.getScores().stream()
                .map(s -> new ScoreSummaryDto(s.getCriterionId(), s.getScore()))
                .toList();
        return new PeerEvalSummaryDto(
                eval.getId(),
                eval.getEvaluateeId(),
                scores,
                eval.getPublicComment(),
                eval.getPrivateComment(),
                eval.getSubmittedAt()
        );
    }
}
