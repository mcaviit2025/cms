package com.example.cms.repository;

import com.example.cms.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    // Find events by active status
    List<Event> findByIsActiveTrue();

    // Find events by isActive = false (soft-deleted)
    List<Event> findByIsActiveFalse();

    // Find upcoming events (date >= current date)
    List<Event> findByEventDateAfter(LocalDate date);

    // Find past events (date < current date)
    List<Event> findByEventDateBefore(LocalDate date);

    // Find events by category
    List<Event> findByCategoryIgnoreCase(String category);

    // Find events by participation type
    List<Event> findByParticipationTypeIgnoreCase(String participationType);

    // Find events by date range
    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    // Search events by name (contains)
    List<Event> findByEventNameContainingIgnoreCase(String eventName);

    // Count events by category
    long countByCategory(String category);

    // Soft delete event (set isActive = false)
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.isActive = false WHERE e.eventId = :eventId")
    void softDelete(@Param("eventId") int eventId);

    // Restore soft-deleted event
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.isActive = true WHERE e.eventId = :eventId")
    void restore(@Param("eventId") int eventId);

    // Get total revenue for an event (sum of approved registrations)
    @Query("SELECT SUM(e.fee) FROM Event e WHERE e.eventId = :eventId")
    Double getTotalRevenueByEventId(@Param("eventId") int eventId);
}