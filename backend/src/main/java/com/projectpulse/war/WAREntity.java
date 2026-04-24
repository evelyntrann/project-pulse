package com.projectpulse.war;

import com.projectpulse.team.TeamEntity;
import com.projectpulse.user.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wars", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "week_start_date"})
})
public class WAREntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT | SUBMITTED

    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "war", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ActivityEntity> activities = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getStudent() { return student; }
    public void setStudent(UserEntity student) { this.student = student; }

    public TeamEntity getTeam() { return team; }
    public void setTeam(TeamEntity team) { this.team = team; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<ActivityEntity> getActivities() { return activities; }
    public void setActivities(List<ActivityEntity> activities) { this.activities = activities; }
}
