package com.projectpulse.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    @Query("SELECT s FROM SectionEntity s LEFT JOIN FETCH s.teams WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) ORDER BY s.name DESC")
    List<SectionEntity> findByNameContaining(@Param("name") String name);
}
