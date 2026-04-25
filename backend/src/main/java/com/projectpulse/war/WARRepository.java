package com.projectpulse.war;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WARRepository extends JpaRepository<WAREntity, Long> {

    Optional<WAREntity> findByStudentIdAndWeekStartDate(Long studentId, LocalDate weekStartDate);

    List<WAREntity> findAllByStudentId(Long studentId);

    List<WAREntity> findAllByTeamId(Long teamId);

    List<WAREntity> findAllByTeamIdAndWeekStartDate(Long teamId, LocalDate weekStartDate);

    @Query("SELECT DISTINCT w FROM WAREntity w JOIN FETCH w.student LEFT JOIN FETCH w.activities " +
           "WHERE w.team.id = :teamId AND w.weekStartDate = :weekStartDate")
    List<WAREntity> findByTeamIdAndWeekStartDateWithActivities(
            @Param("teamId") Long teamId,
            @Param("weekStartDate") LocalDate weekStartDate
    );
}
