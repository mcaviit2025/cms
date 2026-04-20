package com.example.cms.repository;

import com.example.cms.entity.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Integer> {

    List<EvaluationCriteria> findByEventIdOrderByDisplayOrderAsc(Integer eventId);
}