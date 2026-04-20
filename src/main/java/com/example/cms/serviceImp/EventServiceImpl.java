package com.example.cms.serviceImp;

import com.example.cms.entity.Event;
import com.example.cms.repository.EventRepository;
import com.example.cms.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository repo;

    @Override
    public Event saveEvent(Event event) {

        // if SOLO → no group size
        if (event.getParticipationType().equals("Solo")) {
            event.setGroupSize(null);
        }

        return repo.save(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return repo.findAll();
    }

    @Override
    public Event getEventById(int id) {
        return repo.findById(id).orElse(null);
    }
    @Override
    public List<Event> getAllEventsUnique(){
        return repo.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Event::getEventName,
                        e -> e,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
    }
}