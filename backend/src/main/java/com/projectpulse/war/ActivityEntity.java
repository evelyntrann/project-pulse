package com.projectpulse.war;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "activities")
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "war_id", nullable = false)
    private WAREntity war;

    @Column(nullable = false)
    private String category;

    @Column(name = "planned_activity", nullable = false)
    private String plannedActivity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal plannedHours;

    @Column(precision = 5, scale = 2)
    private BigDecimal actualHours;

    private String status; // IN_PROGRESS | UNDER_TESTING | DONE

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WAREntity getWar() { return war; }
    public void setWar(WAREntity war) { this.war = war; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPlannedActivity() { return plannedActivity; }
    public void setPlannedActivity(String plannedActivity) { this.plannedActivity = plannedActivity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }

    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
