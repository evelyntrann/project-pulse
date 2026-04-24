package com.projectpulse.war;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {

    List<ActivityEntity> findAllByWarId(Long warId);

    Optional<ActivityEntity> findByIdAndWarId(Long id, Long warId);
}
