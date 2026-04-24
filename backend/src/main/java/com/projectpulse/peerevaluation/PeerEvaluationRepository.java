package com.projectpulse.peerevaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluationEntity, Long> {

    // Used by getContext and getSubmittedWeeks
    List<PeerEvaluationEntity> findByEvaluatorIdAndWeekStartDate(Long evaluatorId, LocalDate weekStartDate);

    // Used by submitEvaluations (upsert lookup)
    Optional<PeerEvaluationEntity> findByEvaluatorIdAndEvaluateeIdAndWeekStartDate(
            Long evaluatorId, Long evaluateeId, LocalDate weekStartDate);

    // Used by the week-status dropdown (which weeks has this student already submitted)
    @Query("SELECT DISTINCT pe.weekStartDate FROM PeerEvaluationEntity pe WHERE pe.evaluatorId = :evaluatorId")
    List<LocalDate> findSubmittedWeeksByEvaluatorId(@Param("evaluatorId") Long evaluatorId);

    // Used by report endpoints (UC-29, 31, 33)
    List<PeerEvaluationEntity> findByEvaluateeIdAndWeekStartDate(Long evaluateeId, LocalDate weekStartDate);
    List<PeerEvaluationEntity> findByTeamIdAndWeekStartDate(Long teamId, LocalDate weekStartDate);
    List<PeerEvaluationEntity> findByTeamId(Long teamId);
}
