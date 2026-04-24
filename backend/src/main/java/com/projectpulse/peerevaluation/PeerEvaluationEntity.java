package com.projectpulse.peerevaluation;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peer_evaluations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"evaluator_id", "evaluatee_id", "week_start_date"})
})
public class PeerEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain Long columns — same pattern as SectionEntity.rubricId.
    // Avoids cross-package @ManyToOne to UserEntity for a simple FK lookup.
    @Column(name = "evaluator_id", nullable = false)
    private Long evaluatorId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Column(columnDefinition = "TEXT")
    private String publicComment;

    @Column(columnDefinition = "TEXT")
    private String privateComment;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    // orphanRemoval = true so clearing this list deletes old score rows
    // before we insert the replacement set on edits.
    @OneToMany(mappedBy = "peerEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeerEvaluationScoreEntity> scores = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }

    public Long getEvaluateeId() { return evaluateeId; }
    public void setEvaluateeId(Long evaluateeId) { this.evaluateeId = evaluateeId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public String getPublicComment() { return publicComment; }
    public void setPublicComment(String publicComment) { this.publicComment = publicComment; }

    public String getPrivateComment() { return privateComment; }
    public void setPrivateComment(String privateComment) { this.privateComment = privateComment; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<PeerEvaluationScoreEntity> getScores() { return scores; }
    public void setScores(List<PeerEvaluationScoreEntity> scores) { this.scores = scores; }
}
