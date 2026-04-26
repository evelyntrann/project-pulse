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

    // Used by report endpoints (UC-29, 31, 33) — JOIN FETCH avoids N+1 on scores
    @Query("SELECT pe FROM PeerEvaluationEntity pe LEFT JOIN FETCH pe.scores " +
           "WHERE pe.evaluateeId = :evaluateeId AND pe.weekStartDate = :weekStartDate")
    List<PeerEvaluationEntity> findByEvaluateeIdAndWeekStartDateWithScores(
            @Param("evaluateeId") Long evaluateeId,
            @Param("weekStartDate") LocalDate weekStartDate);

    @Query("SELECT pe FROM PeerEvaluationEntity pe LEFT JOIN FETCH pe.scores " +
           "WHERE pe.teamId = :teamId AND pe.weekStartDate = :weekStartDate")
    List<PeerEvaluationEntity> findByTeamIdAndWeekStartDateWithScores(
            @Param("teamId") Long teamId,
            @Param("weekStartDate") LocalDate weekStartDate);

    @Query("SELECT pe FROM PeerEvaluationEntity pe LEFT JOIN FETCH pe.scores " +
           "WHERE pe.evaluateeId = :evaluateeId AND pe.weekStartDate IN :weeks")
    List<PeerEvaluationEntity> findByEvaluateeIdAndWeekStartDateInWithScores(
            @Param("evaluateeId") Long evaluateeId,
            @Param("weeks") List<LocalDate> weeks
    );

    List<PeerEvaluationEntity> findByTeamId(Long teamId);

    @Query("SELECT pe FROM PeerEvaluationEntity pe LEFT JOIN FETCH pe.scores " +
           "WHERE pe.teamId IN :teamIds AND pe.weekStartDate = :weekStartDate")
    List<PeerEvaluationEntity> findByTeamIdInAndWeekStartDateWithScores(
            @Param("teamIds") List<Long> teamIds,
            @Param("weekStartDate") LocalDate weekStartDate);
}
