package com.projectpulse.peerevaluation;

import jakarta.persistence.*;

@Entity
@Table(name = "peer_evaluation_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"peer_evaluation_id", "criterion_id"})
})
public class PeerEvaluationScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_evaluation_id", nullable = false)
    private PeerEvaluationEntity peerEvaluation;

    // Plain Long — avoids a dependency on rubric.CriterionEntity.
    // The service validates criterion IDs against the section rubric.
    @Column(name = "criterion_id", nullable = false)
    private Long criterionId;

    @Column(nullable = false)
    private int score;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PeerEvaluationEntity getPeerEvaluation() { return peerEvaluation; }
    public void setPeerEvaluation(PeerEvaluationEntity peerEvaluation) { this.peerEvaluation = peerEvaluation; }

    public Long getCriterionId() { return criterionId; }
    public void setCriterionId(Long criterionId) { this.criterionId = criterionId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
