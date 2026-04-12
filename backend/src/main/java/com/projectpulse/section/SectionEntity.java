package com.projectpulse.section;

import com.projectpulse.team.TeamEntity;
import com.projectpulse.user.UserEntity;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sections")
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(name = "rubric_id")
    private Long rubricId;

    @Column(nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private DayOfWeek warWeeklyDueDay;

    private LocalTime warDueTime;

    @Enumerated(EnumType.STRING)
    private DayOfWeek peerEvaluationWeeklyDueDay;

    private LocalTime peerEvaluationDueTime;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<TeamEntity> teams = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "section_students",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<UserEntity> enrolledStudents = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "section_instructors",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private Set<UserEntity> enrolledInstructors = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Long getRubricId() { return rubricId; }
    public void setRubricId(Long rubricId) { this.rubricId = rubricId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public DayOfWeek getWarWeeklyDueDay() { return warWeeklyDueDay; }
    public void setWarWeeklyDueDay(DayOfWeek warWeeklyDueDay) { this.warWeeklyDueDay = warWeeklyDueDay; }

    public LocalTime getWarDueTime() { return warDueTime; }
    public void setWarDueTime(LocalTime warDueTime) { this.warDueTime = warDueTime; }

    public DayOfWeek getPeerEvaluationWeeklyDueDay() { return peerEvaluationWeeklyDueDay; }
    public void setPeerEvaluationWeeklyDueDay(DayOfWeek peerEvaluationWeeklyDueDay) { this.peerEvaluationWeeklyDueDay = peerEvaluationWeeklyDueDay; }

    public LocalTime getPeerEvaluationDueTime() { return peerEvaluationDueTime; }
    public void setPeerEvaluationDueTime(LocalTime peerEvaluationDueTime) { this.peerEvaluationDueTime = peerEvaluationDueTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TeamEntity> getTeams() { return teams; }
    public void setTeams(List<TeamEntity> teams) { this.teams = teams; }

    public List<UserEntity> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(List<UserEntity> enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    public Set<UserEntity> getEnrolledInstructors() { return enrolledInstructors; }
    public void setEnrolledInstructors(Set<UserEntity> enrolledInstructors) { this.enrolledInstructors = enrolledInstructors; }
}
