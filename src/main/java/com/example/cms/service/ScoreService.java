package com.example.cms.service;

import com.example.cms.entity.Score;
import java.util.List;
import java.util.Optional;

public interface ScoreService {

    // Save or update score
    Score saveScore(Score score);

    // Get score by ID
    Optional<Score> getScoreById(Integer id);

    // Get all scores for a specific registration
    List<Score> getScoresByRegistrationId(Integer registrationId);

    // Get all scores for a specific event
    List<Score> getScoresByEventId(Integer eventId);

    // Get all scores by a specific judge for an event
    List<Score> getScoresByEventIdAndJudgeId(Integer eventId, Integer judgeId);

    // Get score by registration and judge (check if judge already evaluated)
    Optional<Score> getScoreByRegistrationAndJudge(Integer registrationId, Integer judgeId);

    // Delete score
    void deleteScore(Integer id);

    // Delete all scores for a registration
    void deleteScoresByRegistrationId(Integer registrationId);

    // Delete all scores for an event
    void deleteScoresByEventId(Integer eventId);

    // Get total score for a registration (sum of all judges or latest)
    Integer getTotalScoreForRegistration(Integer registrationId);

    // Get average score for a registration (across all judges)
    Double getAverageScoreForRegistration(Integer registrationId);

    // Check if a registration has been evaluated by a specific judge
    boolean isEvaluatedByJudge(Integer registrationId, Integer judgeId);

    // Get count of evaluated registrations for an event
    long getEvaluatedCountByEventId(Integer eventId);

    // Get count of evaluated registrations by a specific judge for an event
    long getEvaluatedCountByEventIdAndJudgeId(Integer eventId, Integer judgeId);

    // Get all scores with judge details (for report)
    List<Score> getScoresWithJudgeDetails(Integer eventId);
}