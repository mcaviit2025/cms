package com.example.cms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_judges")
public class EventJudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "judge_id")
    private Integer judgeId;

    // ✅ @OnDelete CASCADE → deleting an event deletes its event_judge rows at DB level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    // ✅ @OnDelete CASCADE → deleting a judge deletes its event_judge rows at DB level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Judge judge;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    public EventJudge() {}

    public EventJudge(Integer eventId, Integer judgeId) {
        this.eventId = eventId;
        this.judgeId = judgeId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public Integer getJudgeId() { return judgeId; }
    public void setJudgeId(Integer judgeId) { this.judgeId = judgeId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) {
        this.event = event;
        if (event != null) this.eventId = event.getEventId();
    }

    public Judge getJudge() { return judge; }
    public void setJudge(Judge judge) {
        this.judge = judge;
        if (judge != null) this.judgeId = judge.getId();
    }
}