package com.projectpulse.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    @Query("SELECT t FROM TeamEntity t JOIN FETCH t.section s WHERE " +
           "(:sectionId IS NULL OR s.id = :sectionId) AND " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "ORDER BY s.name DESC, t.name ASC")
    List<TeamEntity> findByFilters(
            @Param("sectionId") @Nullable Long sectionId,
            @Param("name") @Nullable String name
    );

    boolean existsByName(String name);
}
