package com.example.cms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "registration_id", nullable = false)
    private Integer registrationId;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @Column(name = "judge_id", nullable = false)
    private Integer judgeId;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "scores_data", length = 2000)
    private String scoresData;  // JSON format: {"criteriaId1": 25, "criteriaId2": 30}

    @Column(length = 500)
    private String comments;

    @Column(name = "is_finalized")
    private Boolean isFinalized = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Score() {}

    public Score(Integer registrationId, Integer eventId, Integer judgeId) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.judgeId = judgeId;
        this.totalScore = 0;
        this.isFinalized = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRegistrationId() { return registrationId; }
    public void setRegistrationId(Integer registrationId) { this.registrationId = registrationId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public Integer getJudgeId() { return judgeId; }
    public void setJudgeId(Integer judgeId) { this.judgeId = judgeId; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getScoresData() { return scoresData; }
    public void setScoresData(String scoresData) { this.scoresData = scoresData; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Boolean getIsFinalized() { return isFinalized; }
    public void setIsFinalized(Boolean isFinalized) { this.isFinalized = isFinalized; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}