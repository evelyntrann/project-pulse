package com.projectpulse.war;

import com.projectpulse.section.ActiveWeekRepository;
import com.projectpulse.team.TeamEntity;
import com.projectpulse.team.TeamRepository;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import com.projectpulse.war.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WARService {

    private final WARRepository warRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final TeamRepository teamRepository;

    public WARService(WARRepository warRepository,
                      ActivityRepository activityRepository,
                      UserRepository userRepository,
                      ActiveWeekRepository activeWeekRepository,
                      TeamRepository teamRepository) {
        this.warRepository = warRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.teamRepository = teamRepository;
    }

    public List<LocalDate> getAvailableWeeks(Long studentId) {
        Long sectionId = userRepository.findSectionIdByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("You are not enrolled in a section."));
        LocalDate today = LocalDate.now();
        return activeWeekRepository.findBySectionIdOrderByWeekStartDate(sectionId)
                .stream()
                .filter(w -> w.isActive() && !w.getWeekStartDate().isAfter(today))
                .map(w -> w.getWeekStartDate())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WARSummaryResponse> getWARs(Long studentId) {
        return warRepository.findAllByStudentId(studentId)
                .stream()
                .map(war -> new WARSummaryResponse(
                        war.getId(),
                        war.getWeekStartDate(),
                        war.getStatus(),
                        war.getSubmittedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public WARDetailResponse getWAR(Long studentId, Long warId) {
        WAREntity war = warRepository.findById(warId)
                .orElseThrow(() -> new NoSuchElementException("WAR not found"));
        verifyOwnership(war, studentId);
        return toDetailResponse(war);
    }

    @Transactional
    public WARDetailResponse createWAR(Long studentId, LocalDate weekStartDate) {
        return warRepository.findByStudentIdAndWeekStartDate(studentId, weekStartDate)
                .map(this::toDetailResponse)
                .orElseGet(() -> {
                    Long teamId = userRepository.findTeamIdByStudentId(studentId)
                            .orElseThrow(() -> new IllegalStateException("You are not assigned to a team."));
                    UserEntity student = userRepository.findById(studentId)
                            .orElseThrow(() -> new NoSuchElementException("Student not found"));
                    TeamEntity team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new NoSuchElementException("Team not found"));
                    WAREntity war = new WAREntity();
                    war.setStudent(student);
                    war.setTeam(team);
                    war.setWeekStartDate(weekStartDate);
                    return toDetailResponse(warRepository.save(war));
                });
    }

    @Transactional
    public WARDetailResponse addActivity(Long studentId, Long warId, ActivityRequest request) {
        WAREntity war = warRepository.findById(warId)
                .orElseThrow(() -> new NoSuchElementException("WAR not found"));
        verifyOwnership(war, studentId);
        if ("SUBMITTED".equals(war.getStatus())) {
            throw new IllegalStateException("Cannot modify a submitted WAR.");
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setWar(war);
        activity.setCategory(request.category());
        activity.setPlannedActivity(request.plannedActivity());
        activity.setDescription(request.description());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        activity.setStatus(request.status() != null ? request.status() : "IN_PROGRESS");
        war.getActivities().add(activity);
        warRepository.save(war);
        return toDetailResponse(war);
    }

    @Transactional
    public WARDetailResponse updateActivity(Long studentId, Long warId, Long activityId, ActivityRequest request) {
        WAREntity war = warRepository.findById(warId)
                .orElseThrow(() -> new NoSuchElementException("WAR not found"));
        verifyOwnership(war, studentId);
        if ("SUBMITTED".equals(war.getStatus())) {
            throw new IllegalStateException("Cannot modify a submitted WAR.");
        }
        ActivityEntity activity = activityRepository.findByIdAndWarId(activityId, warId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));
        activity.setCategory(request.category());
        activity.setPlannedActivity(request.plannedActivity());
        activity.setDescription(request.description());
        activity.setPlannedHours(request.plannedHours());
        activity.setActualHours(request.actualHours());
        if (request.status() != null) activity.setStatus(request.status());
        activityRepository.save(activity);
        return toDetailResponse(war);
    }

    @Transactional
    public WARDetailResponse deleteActivity(Long studentId, Long warId, Long activityId) {
        WAREntity war = warRepository.findById(warId)
                .orElseThrow(() -> new NoSuchElementException("WAR not found"));
        verifyOwnership(war, studentId);
        if ("SUBMITTED".equals(war.getStatus())) {
            throw new IllegalStateException("Cannot modify a submitted WAR.");
        }
        boolean removed = war.getActivities().removeIf(a -> a.getId().equals(activityId));
        if (!removed) {
            throw new NoSuchElementException("Activity not found");
        }
        warRepository.save(war);
        return toDetailResponse(war);
    }

    @Transactional
    public WARDetailResponse submitWAR(Long studentId, Long warId) {
        WAREntity war = warRepository.findById(warId)
                .orElseThrow(() -> new NoSuchElementException("WAR not found"));
        verifyOwnership(war, studentId);
        if ("SUBMITTED".equals(war.getStatus())) {
            throw new IllegalStateException("WAR has already been submitted.");
        }
        war.setStatus("SUBMITTED");
        war.setSubmittedAt(LocalDateTime.now());
        return toDetailResponse(warRepository.save(war));
    }

    private void verifyOwnership(WAREntity war, Long studentId) {
        if (!war.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Access denied.");
        }
    }

    private WARDetailResponse toDetailResponse(WAREntity war) {
        List<ActivityResponse> activities = war.getActivities().stream()
                .map(a -> new ActivityResponse(
                        a.getId(), a.getCategory(), a.getPlannedActivity(),
                        a.getDescription(), a.getPlannedHours(), a.getActualHours(), a.getStatus()))
                .toList();
        return new WARDetailResponse(
                war.getId(), war.getWeekStartDate(), war.getStatus(),
                war.getSubmittedAt(), activities);
    }
}
