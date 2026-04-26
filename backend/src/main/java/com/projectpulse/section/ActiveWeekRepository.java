package com.projectpulse.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActiveWeekRepository extends JpaRepository<ActiveWeekEntity, Long> {

    List<ActiveWeekEntity> findBySectionIdOrderByWeekStartDate(Long sectionId);

    @Modifying
    @Query("DELETE FROM ActiveWeekEntity w WHERE w.sectionId = :sectionId")
    void deleteBySectionId(@Param("sectionId") Long sectionId);
}
