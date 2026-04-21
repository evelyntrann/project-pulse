package com.projectpulse.team;

import com.projectpulse.section.SectionEntity;
import com.projectpulse.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "website_url")
    private String websiteUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @ManyToMany
    @JoinTable(
            name = "team_students",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<UserEntity> students = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "team_instructors",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private Set<UserEntity> instructors = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public SectionEntity getSection() { return section; }
    public void setSection(SectionEntity section) { this.section = section; }

    public Set<UserEntity> getStudents() { return students; }
    public void setStudents(Set<UserEntity> students) { this.students = students; }

    public Set<UserEntity> getInstructors() { return instructors; }
    public void setInstructors(Set<UserEntity> instructors) { this.instructors = instructors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
