package com.example.cms.repository;

import com.example.cms.entity.EventJudge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EventJudgeRepository extends JpaRepository<EventJudge, Integer> {
    List<EventJudge> findByJudgeId(Integer judgeId);
    List<EventJudge> findByEventId(Integer eventId);

    @Query("SELECT e.eventId FROM EventJudge e WHERE e.judgeId = :judgeId")
    List<Integer> findEventIdsByJudgeId(Integer judgeId);



    @Modifying
    @Transactional
    void deleteByEventIdAndJudgeId(Integer eventId, Integer judgeId);
    @Query("SELECT e.judgeId FROM EventJudge e WHERE e.eventId = :eventId")
    List<Integer> findJudgeIdsByEventId(@Param("eventId") Integer eventId);
    boolean existsByEventIdAndJudgeId(Integer eventId, Integer judgeId);
}