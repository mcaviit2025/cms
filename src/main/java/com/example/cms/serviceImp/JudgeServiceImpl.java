package com.example.cms.serviceimpl;

import com.example.cms.dto.AssignJudgeDTO;
import com.example.cms.dto.JudgeDTO;
import com.example.cms.dto.JudgeLoginRequest;
import com.example.cms.entity.Event;
import com.example.cms.entity.EventJudge;
import com.example.cms.entity.Judge;
import com.example.cms.repository.EventJudgeRepository;
import com.example.cms.repository.JudgeRepository;
import com.example.cms.service.EventService;
import com.example.cms.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Autowired
    private JudgeRepository judgeRepository;

    @Autowired
    private EventJudgeRepository eventJudgeRepository;

    @Override
    public Judge createJudge(JudgeDTO judgeDTO) {
        if (judgeRepository.existsByUsername(judgeDTO.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        Judge judge = new Judge();
        judge.setUsername(judgeDTO.getUsername());
        judge.setPassword(judgeDTO.getPassword()); // Plain text
        judge.setFullName(judgeDTO.getFullName());
        judge.setIsActive(true);

        return judgeRepository.save(judge);
    }

    @Override
    public List<Judge> getAllJudges() {
        return judgeRepository.findAll();
    }

    @Override
    public Judge getJudgeById(Integer id) {
        return judgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Judge not found"));
    }

    @Override
    public Judge updateJudge(Integer id, JudgeDTO judgeDTO) {
        Judge judge = getJudgeById(id);
        judge.setFullName(judgeDTO.getFullName());
        judge.setUsername(judgeDTO.getUsername());
        if (judgeDTO.getPassword() != null && !judgeDTO.getPassword().isEmpty()) {
            judge.setPassword(judgeDTO.getPassword());
        }
        return judgeRepository.save(judge);
    }

    @Override
    public void deleteJudge(Integer id) {
        judgeRepository.deleteById(id);
    }

    @Override
    public boolean toggleJudgeStatus(Integer id) {
        Judge judge = getJudgeById(id);
        judge.setIsActive(!judge.getIsActive());
        judgeRepository.save(judge);
        return judge.getIsActive();
    }

    @Override
    public boolean resetPassword(Integer id, String newPassword) {
        Judge judge = getJudgeById(id);
        judge.setPassword(newPassword);
        judgeRepository.save(judge);
        return true;
    }

    @Override
    public boolean authenticate(JudgeLoginRequest loginRequest) {
        Optional<Judge> judgeOpt = judgeRepository.findByUsername(loginRequest.getUsername());
        if (judgeOpt.isPresent()) {
            Judge judge = judgeOpt.get();
            return judge.getIsActive() && judge.getPassword().equals(loginRequest.getPassword());
        }
        return false;
    }

    @Override
    @Transactional
    public void assignJudgeToEvent(AssignJudgeDTO assignDTO) {
        if (!eventJudgeRepository.existsByEventIdAndJudgeId(assignDTO.getEventId(), assignDTO.getJudgeId())) {
            EventJudge eventJudge = new EventJudge(assignDTO.getEventId(), assignDTO.getJudgeId());
            eventJudgeRepository.save(eventJudge);
        }
    }

    @Override
    @Transactional
    public void removeJudgeFromEvent(Integer eventId, Integer judgeId) {
        eventJudgeRepository.deleteByEventIdAndJudgeId(eventId, judgeId);
    }

    @Override
    public List<Integer> getAssignedEventIds(Integer judgeId) {
        return eventJudgeRepository.findEventIdsByJudgeId(judgeId);
    }

    @Override
    public List<Judge> getAvailableJudgesForEvent(Integer eventId) {
        List<Integer> assignedJudgeIds = eventJudgeRepository.findByEventId(eventId)
                .stream()
                .map(EventJudge::getJudgeId)
                .collect(Collectors.toList());

        return judgeRepository.findAll().stream()
                .filter(judge -> !assignedJudgeIds.contains(judge.getId()))
                .collect(Collectors.toList());
    }
    @Autowired
    EventService eventService;
    @Override
    public Judge getJudgeWithEvents(Integer id) {
        Judge judge = getJudgeById(id);
        List<Integer> eventIds = eventJudgeRepository.findEventIdsByJudgeId(id);
        List<Event> assignedEvents = eventIds.stream()
                .map(eventService::getEventById)
                .collect(Collectors.toList());
        judge.setAssignedEvents(assignedEvents);
        return judge;
    }

}