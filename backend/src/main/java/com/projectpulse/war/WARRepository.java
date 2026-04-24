package com.projectpulse.war;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WARRepository extends JpaRepository<WAREntity, Long> {

    Optional<WAREntity> findByStudentIdAndWeekStartDate(Long studentId, LocalDate weekStartDate);

    List<WAREntity> findAllByStudentId(Long studentId);

    List<WAREntity> findAllByTeamId(Long teamId);

    List<WAREntity> findAllByTeamIdAndWeekStartDate(Long teamId, LocalDate weekStartDate);
}
