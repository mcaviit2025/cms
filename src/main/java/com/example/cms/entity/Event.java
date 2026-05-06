package com.example.cms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventId;

    private String eventName;
    private String description;
    private LocalDate eventDate;
    private String venue;
    private double fee;
    private String category;
    private String participationType;
    private int maxParticipants;
    private Integer groupSize;
    private boolean isActive = true;
    private LocalDate createdAt;

    // Relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EvaluationCriteria> evaluationCriteria = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventJudge> eventJudges = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Score> scores = new ArrayList<>();

    // ========== TRANSIENT FIELDS FOR JUDGE DASHBOARD ==========
    @Transient
    private int totalParticipants;

    @Transient
    private int evaluatedByMe;

    @Transient
    private int evaluatedByOthers;

    @Transient
    private int pendingParticipants;

    @Transient
    private List<Judge> assignedJudges;

    @Transient
    private int approvedParticipants;

    @Transient
    private double totalCollected;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }

    // Constructors
    public Event() {}

    // ========== GETTERS AND SETTERS ==========

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getParticipationType() { return participationType; }
    public void setParticipationType(String participationType) { this.participationType = participationType; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public List<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }

    public List<EvaluationCriteria> getEvaluationCriteria() { return evaluationCriteria; }
    public void setEvaluationCriteria(List<EvaluationCriteria> evaluationCriteria) { this.evaluationCriteria = evaluationCriteria; }

    public List<EventJudge> getEventJudges() { return eventJudges; }
    public void setEventJudges(List<EventJudge> eventJudges) { this.eventJudges = eventJudges; }

    public List<Score> getScores() { return scores; }
    public void setScores(List<Score> scores) { this.scores = scores; }

    // ========== TRANSIENT FIELDS GETTERS/SETTERS ==========

    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }

    public int getEvaluatedByMe() { return evaluatedByMe; }
    public void setEvaluatedByMe(int evaluatedByMe) { this.evaluatedByMe = evaluatedByMe; }

    public int getEvaluatedByOthers() { return evaluatedByOthers; }
    public void setEvaluatedByOthers(int evaluatedByOthers) { this.evaluatedByOthers = evaluatedByOthers; }

    public int getPendingParticipants() { return pendingParticipants; }
    public void setPendingParticipants(int pendingParticipants) { this.pendingParticipants = pendingParticipants; }

    public List<Judge> getAssignedJudges() { return assignedJudges; }
    public void setAssignedJudges(List<Judge> assignedJudges) { this.assignedJudges = assignedJudges; }

    public int getApprovedParticipants() { return approvedParticipants; }
    public void setApprovedParticipants(int approvedParticipants) { this.approvedParticipants = approvedParticipants; }

    public double getTotalCollected() { return totalCollected; }
    public void setTotalCollected(double totalCollected) { this.totalCollected = totalCollected; }
}