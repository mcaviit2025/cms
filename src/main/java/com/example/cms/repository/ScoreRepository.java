package com.example.cms.repository;

import com.example.cms.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Integer> {

    Optional<Score> findByRegistrationIdAndJudgeId(Integer registrationId, Integer judgeId);

    List<Score> findByEventIdAndJudgeId(Integer eventId, Integer judgeId);

    List<Score> findByEventId(Integer eventId);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.eventId = :eventId AND s.judgeId = :judgeId")
    long countByEventIdAndJudgeId(Integer eventId, Integer judgeId);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.eventId = :eventId")
    long countByEventId(Integer eventId);
}