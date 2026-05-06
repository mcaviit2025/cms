package com.example.cms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    // ✅ nullable = true → allows SET NULL when judge is deleted
    @Column(name = "judge_id", nullable = true)
    private Integer judgeId;

    // ✅ @OnDelete CASCADE → deleting a registration deletes its scores at DB level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Registration registration;

    // ✅ @OnDelete CASCADE → deleting an event deletes its scores at DB level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    // ✅ @OnDelete SET_NULL → deleting a judge sets judge_id to NULL, score row is kept
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", insertable = false, updatable = false, nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Judge judge;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "scores_data", length = 2000)
    private String scoresData;

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

    public Score() {}

    public Score(Integer registrationId, Integer eventId, Integer judgeId) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.judgeId = judgeId;
        this.totalScore = 0;
        this.isFinalized = false;
    }

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

    public Registration getRegistration() { return registration; }
    public void setRegistration(Registration registration) {
        this.registration = registration;
        if (registration != null) this.registrationId = registration.getId();
    }

    public Event getEvent() { return event; }
    public void setEvent(Event event) {
        this.event = event;
        if (event != null) this.eventId = event.getEventId();
    }

    public Judge getJudge() { return judge; }
    public void setJudge(Judge judge) {
        this.judge = judge;
        this.judgeId = (judge != null) ? judge.getId() : null;
    }
}