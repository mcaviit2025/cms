package com.example.cms.serviceImp;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.repository.EventRepository;
import com.example.cms.repository.RegistrationRepository;
import com.example.cms.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(int eventId) {
        return eventRepository.findById(eventId).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getActiveEvents() {
        return eventRepository.findByIsActiveTrue();
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateAfter(LocalDate.now());
    }

    @Override
    @Transactional
    public void deleteEvent(int eventId) {
        Event event = getEventById(eventId);
        if (event != null) {
            // The cascade will automatically handle:
            // - All registrations for this event
            // - All payments for those registrations
            // - All scores for this event
            // - All evaluation criteria for this event
            // - All judge assignments for this event
            eventRepository.delete(event);
        }
    }

    @Override
    @Transactional
    public void softDeleteEvent(int eventId) {
        Event event = getEventById(eventId);
        if (event != null) {
            event.setActive(false);
            eventRepository.save(event);
        }
    }

    @Override
    @Transactional
    public void restoreEvent(int eventId) {
        Event event = getEventById(eventId);
        if (event != null) {
            event.setActive(true);
            eventRepository.save(event);
        }
    }

    @Override
    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    public List<Event> getEventsByParticipationType(String participationType) {
        return eventRepository.findByParticipationTypeIgnoreCase(participationType);
    }

    @Override
    public boolean eventExists(int eventId) {
        return eventRepository.existsById(eventId);
    }

    @Override
    public long getTotalEventCount() {
        return eventRepository.count();
    }

    @Override
    public double getTotalRevenue() {
        List<Registration> allApprovedRegistrations = registrationRepository.findByStatus("APPROVED");

        double totalRevenue = 0;
        for (Registration reg : allApprovedRegistrations) {
            Event event = getEventById(reg.getEventId());
            if (event != null) {
                if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                    int memberCount = 1;
                    if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                        memberCount += reg.getMemberNames().split(",").length;
                    }
                    totalRevenue += event.getFee() * memberCount;
                } else {
                    totalRevenue += event.getFee();
                }
            }
        }
        return totalRevenue;
    }

    @Override
    public int getTotalParticipantsCount() {
        List<Registration> allRegistrations = registrationRepository.findAll();
        int total = 0;
        for (Registration reg : allRegistrations) {
            if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                int memberCount = 1;
                if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                    memberCount += reg.getMemberNames().split(",").length;
                }
                total += memberCount;
            } else {
                total += 1;
            }
        }
        return total;
    }

    @Override
    @Transactional
    public void updateEventStatus(int eventId, boolean isActive) {
        Event event = getEventById(eventId);
        if (event != null) {
            event.setActive(isActive);
            eventRepository.save(event);
        }
    }
}