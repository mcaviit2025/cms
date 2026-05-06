package com.example.cms.repository;

import com.example.cms.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    // Find registrations by event ID
    List<Registration> findByEventId(int eventId);

    // Find registrations by status (APPROVED, PENDING, REJECTED)
    List<Registration> findByStatus(String status);

    // Find registrations by event ID and status
    List<Registration> findByEventIdAndStatus(int eventId, String status);

    // Find registration by leader email and event ID (check duplicate)
    boolean existsByLeaderEmailAndEventId(String leaderEmail, int eventId);

    // Check if email exists as member in any team for this event
    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.eventId = :eventId AND r.memberEmails LIKE %:email%")
    boolean existsByEventIdAndMemberEmailsContaining(@Param("eventId") int eventId, @Param("email") String email);

    // Check if leader already registered for event
    boolean existsByEventIdAndLeaderEmail(int eventId, String leaderEmail);

    // Find registration by participant code
    Optional<Registration> findByParticipantCode(String participantCode);

    // Find registrations by college
    List<Registration> findByCollegeIgnoreCase(String college);

    // Find registrations by participation type
    List<Registration> findByParticipationType(String participationType);

    // Get count of registrations by event and status
    long countByEventIdAndStatus(int eventId, String status);

    // Get count by status
    long countByStatus(String status);

    // Find registrations with payment proof uploaded
    List<Registration> findByPaymentProofIsNotNull();

    // Find registrations where UTR number is not null
    List<Registration> findByUtrNumberIsNotNull();

    // Search registrations by leader name (contains)
    List<Registration> findByLeaderNameContainingIgnoreCase(String leaderName);

    // Search registrations by leader email (contains)
    List<Registration> findByLeaderEmailContainingIgnoreCase(String leaderEmail);

    // REMOVE or COMMENT OUT this line - it's causing the error
    // List<Registration> findByEventIdOrderByCreatedAtDesc(int eventId);

    // Alternative: Use this instead
    List<Registration> findByEventIdOrderByIdDesc(int eventId);

    // Get registrations for an event with scores (joined fetch)
    @Query("SELECT DISTINCT r FROM Registration r LEFT JOIN FETCH r.scores WHERE r.eventId = :eventId")
    List<Registration> findByEventIdWithScores(@Param("eventId") int eventId);

    // Get approved registrations count for an event
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.eventId = :eventId AND r.status = 'APPROVED'")
    long countApprovedByEventId(@Param("eventId") int eventId);
}