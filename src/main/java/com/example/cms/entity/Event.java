package com.example.cms.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventId;

    private String eventName;
    private String eventDate;
    private String venue;
    private double fee;
    private String category;
    private String participationType;
    private int maxParticipants;

    // NEW FIELD
    private Integer groupSize;   // nullable (only for group/both)

    private String description;
    @Transient
    private List<Judge> assignedJudges;

    public List<Judge> getAssignedJudges() {
        return assignedJudges;
    }

    public void setAssignedJudges(List<Judge> assignedJudges) {
        this.assignedJudges = assignedJudges;
    }

    // Getters & Setters

    public int getEventId() { return eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Transient
    private int totalParticipants;

    @Transient
    private int evaluatedByMe;

    @Transient
    private int evaluatedByOthers;

    @Transient
    private int pendingParticipants;

    // Getters and Setters
    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }

    public int getEvaluatedByMe() { return evaluatedByMe; }
    public void setEvaluatedByMe(int evaluatedByMe) { this.evaluatedByMe = evaluatedByMe; }

    public int getEvaluatedByOthers() { return evaluatedByOthers; }
    public void setEvaluatedByOthers(int evaluatedByOthers) { this.evaluatedByOthers = evaluatedByOthers; }

    public int getPendingParticipants() { return pendingParticipants; }
    public void setPendingParticipants(int pendingParticipants) { this.pendingParticipants = pendingParticipants; }
}