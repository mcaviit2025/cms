package com.example.cms.service;

import com.example.cms.entity.Event;
import java.util.List;

public interface EventService {

    // Save or update event
    Event saveEvent(Event event);

    // Get event by ID
    Event getEventById(int eventId);

    // Get all events
    List<Event> getAllEvents();

    // Get active events (isActive = true)
    List<Event> getActiveEvents();

    // Get upcoming events (eventDate >= current date)
    List<Event> getUpcomingEvents();

    // Delete event by ID (with cascade)
    void deleteEvent(int eventId);

    // Soft delete (set isActive = false)
    void softDeleteEvent(int eventId);

    // Restore soft-deleted event
    void restoreEvent(int eventId);

    // Get events by category
    List<Event> getEventsByCategory(String category);

    // Get events by participation type
    List<Event> getEventsByParticipationType(String participationType);

    // Check if event exists
    boolean eventExists(int eventId);

    // Get total count of events
    long getTotalEventCount();

    // Get total revenue from all events (from approved registrations)
    double getTotalRevenue();

    // Get total participants across all events
    int getTotalParticipantsCount();

    // Update event status
    void updateEventStatus(int eventId, boolean isActive);
}