package com.example.cms.serviceImp;

import com.example.cms.entity.Score;
import com.example.cms.repository.ScoreRepository;
import com.example.cms.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Override
    public Score saveScore(Score score) {
        if (score.getCreatedAt() == null) {
            score.setCreatedAt(java.time.LocalDateTime.now());
        }
        score.setUpdatedAt(java.time.LocalDateTime.now());
        return scoreRepository.save(score);
    }

    @Override
    public Optional<Score> getScoreById(Integer id) {
        return scoreRepository.findById(id);
    }

    @Override
    public List<Score> getScoresByRegistrationId(Integer registrationId) {
        return scoreRepository.findByRegistrationId(registrationId);
    }

    @Override
    public List<Score> getScoresByEventId(Integer eventId) {
        return scoreRepository.findByEventId(eventId);
    }

    @Override
    public List<Score> getScoresByEventIdAndJudgeId(Integer eventId, Integer judgeId) {
        return scoreRepository.findByEventIdAndJudgeId(eventId, judgeId);
    }

    @Override
    public Optional<Score> getScoreByRegistrationAndJudge(Integer registrationId, Integer judgeId) {
        return scoreRepository.findByRegistrationIdAndJudgeId(registrationId, judgeId);
    }

    @Override
    public void deleteScore(Integer id) {
        scoreRepository.deleteById(id);
    }

    @Override
    public void deleteScoresByRegistrationId(Integer registrationId) {
        List<Score> scores = getScoresByRegistrationId(registrationId);
        scoreRepository.deleteAll(scores);
    }

    @Override
    public void deleteScoresByEventId(Integer eventId) {
        List<Score> scores = getScoresByEventId(eventId);
        scoreRepository.deleteAll(scores);
    }

    @Override
    public Integer getTotalScoreForRegistration(Integer registrationId) {
        List<Score> scores = getScoresByRegistrationId(registrationId);
        return scores.stream()
                .mapToInt(Score::getTotalScore)
                .sum();
    }

    @Override
    public Double getAverageScoreForRegistration(Integer registrationId) {
        List<Score> scores = getScoresByRegistrationId(registrationId);
        return scores.stream()
                .mapToInt(Score::getTotalScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public boolean isEvaluatedByJudge(Integer registrationId, Integer judgeId) {
        return getScoreByRegistrationAndJudge(registrationId, judgeId).isPresent();
    }

    @Override
    public long getEvaluatedCountByEventId(Integer eventId) {
        List<Score> scores = getScoresByEventId(eventId);
        // Count unique registration IDs (each registration counted once even if multiple judges)
        return scores.stream()
                .map(Score::getRegistrationId)
                .distinct()
                .count();
    }

    @Override
    public long getEvaluatedCountByEventIdAndJudgeId(Integer eventId, Integer judgeId) {
        return getScoresByEventIdAndJudgeId(eventId, judgeId).size();
    }

    @Override
    public List<Score> getScoresWithJudgeDetails(Integer eventId) {
        return scoreRepository.findScoresWithJudgeDetails(eventId);
    }
}