package com.example.cms.dto;

import java.util.List;

public class ExportConfigDTO {
    private String fileFormat; // EXCEL or CSV
    private String participantStatus; // ALL, APPROVED, PENDING, REJECTED
    private String paymentStatus; // ALL, PAID, PENDING
    private boolean includeScores;
    private boolean includePaymentDetails;
    private List<String> selectedFields;
    private int eventId;
    private String eventName;

    // Default constructor
    public ExportConfigDTO() {
        this.fileFormat = "EXCEL";
        this.participantStatus = "ALL";
        this.paymentStatus = "ALL";
        this.includeScores = true;
        this.includePaymentDetails = true;
    }

    // Getters and Setters
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public String getParticipantStatus() { return participantStatus; }
    public void setParticipantStatus(String participantStatus) { this.participantStatus = participantStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public boolean isIncludeScores() { return includeScores; }
    public void setIncludeScores(boolean includeScores) { this.includeScores = includeScores; }

    public boolean isIncludePaymentDetails() { return includePaymentDetails; }
    public void setIncludePaymentDetails(boolean includePaymentDetails) { this.includePaymentDetails = includePaymentDetails; }

    public List<String> getSelectedFields() { return selectedFields; }
    public void setSelectedFields(List<String> selectedFields) { this.selectedFields = selectedFields; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}