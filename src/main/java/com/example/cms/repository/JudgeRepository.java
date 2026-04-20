package com.example.cms.repository;

import com.example.cms.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JudgeRepository extends JpaRepository<Judge, Integer> {
    Optional<Judge> findByUsername(String username);
    boolean existsByUsername(String username);
}