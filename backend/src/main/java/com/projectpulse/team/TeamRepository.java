package com.projectpulse.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.section s LEFT JOIN FETCH t.students LEFT JOIN FETCH t.instructors WHERE " +
           "(:sectionId IS NULL OR s.id = :sectionId) AND " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "ORDER BY s.name DESC, t.name ASC")
    List<TeamEntity> findByFiltersWithStudents(
            @Param("sectionId") Long sectionId,
            @Param("name") String name
    );

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.section LEFT JOIN FETCH t.students LEFT JOIN FETCH t.instructors WHERE t.id = :id")
    Optional<TeamEntity> findByIdWithStudents(@Param("id") Long id);

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.section s LEFT JOIN FETCH t.students WHERE s.id = :sectionId AND :student MEMBER OF t.students")
    Optional<TeamEntity> findBySectionAndStudent(@Param("sectionId") Long sectionId, @Param("student") com.projectpulse.user.UserEntity student);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
