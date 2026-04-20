package com.example.cms.service;

import com.example.cms.entity.Event;
import java.util.List;

public interface EventService {
    Event saveEvent(Event event);
    List<Event> getAllEvents();
    Event getEventById(int id);
    List<Event> getAllEventsUnique();
}