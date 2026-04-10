package com.projectpulse.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    @Query("SELECT s FROM SectionEntity s LEFT JOIN FETCH s.teams WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) ORDER BY s.name DESC")
    List<SectionEntity> findByNameContaining(@Param("name") String name);

    @Query("SELECT s FROM SectionEntity s LEFT JOIN FETCH s.teams WHERE s.id = :id")
    Optional<SectionEntity> findByIdWithTeams(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM SectionEntity s LEFT JOIN FETCH s.teams t LEFT JOIN FETCH t.students WHERE s.id = :id")
    Optional<SectionEntity> findByIdWithTeamsAndStudents(@Param("id") Long id);

    @Query("SELECT s FROM SectionEntity s LEFT JOIN FETCH s.enrolledStudents WHERE s.id = :id")
    Optional<SectionEntity> findByIdWithEnrolledStudents(@Param("id") Long id);

    boolean existsByName(String name);
}
