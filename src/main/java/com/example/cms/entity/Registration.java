package com.example.cms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registration")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "event_id")
    private int eventId;

    // ✅ @OnDelete CASCADE here → Hibernate writes ON DELETE CASCADE on this FK
    // So deleting an event automatically deletes all its registrations at DB level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    private String leaderName;
    private String leaderEmail;
    private String phone;
    private String college;
    private String teamName;
    private String emailStatus;

    @Column(length = 2000)
    private String memberNames;

    @Column(length = 2000)
    private String memberEmails;

    private String utrNumber;
    private String paymentProof;
    private String status = "PENDING";
    private String participationType;
    private String participantCode;

    // Delete registration → deletes all its scores
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Score> scores = new ArrayList<>();

    // Delete registration → deletes its payment
    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;



    public Registration() {}

    public Registration(int eventId, String leaderName, String leaderEmail, String phone, String college,
                        String teamName, String memberNames, String memberEmails, String utrNumber,
                        String paymentProof, String status, String participationType, String participantCode,
                        String emailStatus) {
        this.eventId = eventId;
        this.leaderName = leaderName;
        this.leaderEmail = leaderEmail;
        this.phone = phone;
        this.college = college;
        this.teamName = teamName;
        this.memberNames = memberNames;
        this.memberEmails = memberEmails;
        this.utrNumber = utrNumber;
        this.paymentProof = paymentProof;
        this.status = status;
        this.participationType = participationType;
        this.participantCode = participantCode;
        this.emailStatus = emailStatus;
    }

    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) {
        this.event = event;
        if (event != null) this.eventId = event.getEventId();
    }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public String getLeaderEmail() { return leaderEmail; }
    public void setLeaderEmail(String leaderEmail) { this.leaderEmail = leaderEmail; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getMemberNames() { return memberNames; }
    public void setMemberNames(String memberNames) { this.memberNames = memberNames; }

    public String getMemberEmails() { return memberEmails; }
    public void setMemberEmails(String memberEmails) { this.memberEmails = memberEmails; }

    public String getUtrNumber() { return utrNumber; }
    public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }

    public String getPaymentProof() { return paymentProof; }
    public void setPaymentProof(String paymentProof) { this.paymentProof = paymentProof; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getParticipationType() { return participationType; }
    public void setParticipationType(String participationType) { this.participationType = participationType; }

    public String getParticipantCode() { return participantCode; }
    public void setParticipantCode(String participantCode) { this.participantCode = participantCode; }

    public List<Score> getScores() { return scores; }
    public void setScores(List<Score> scores) { this.scores = scores; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) payment.setRegistration(this);
    }

    public void addScore(Score score) {
        scores.add(score);
        score.setRegistration(this);
    }

    public void addPayment(Payment payment) {
        this.payment = payment;
        payment.setRegistration(this);
    }
}