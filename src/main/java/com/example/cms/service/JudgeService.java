package com.example.cms.service;

import com.example.cms.dto.AssignJudgeDTO;
import com.example.cms.dto.JudgeDTO;
import com.example.cms.dto.JudgeLoginRequest;
import com.example.cms.entity.Judge;
import java.util.List;

public interface JudgeService {
    // Judge Management
    Judge createJudge(JudgeDTO judgeDTO);
    List<Judge> getAllJudges();
    Judge getJudgeById(Integer id);
    Judge updateJudge(Integer id, JudgeDTO judgeDTO);
    void deleteJudge(Integer id);
    boolean toggleJudgeStatus(Integer id);
    boolean resetPassword(Integer id, String newPassword);

    // Authentication
    boolean authenticate(JudgeLoginRequest loginRequest);

    // Assignment
    void assignJudgeToEvent(AssignJudgeDTO assignDTO);
    void removeJudgeFromEvent(Integer eventId, Integer judgeId);
    List<Integer> getAssignedEventIds(Integer judgeId);
    List<Judge> getAvailableJudgesForEvent(Integer eventId);
    Judge getJudgeWithEvents(Integer id);
}