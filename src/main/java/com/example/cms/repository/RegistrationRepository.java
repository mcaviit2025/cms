package com.example.cms.repository;

import com.example.cms.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {


    boolean existsByEventIdAndLeaderEmail(int eventId, String leaderEmail);



    boolean existsByEventIdAndMemberEmailsContaining(int eventId, String email);

    List<Registration> findByEventId(int eventId);

    void deleteById(int id);

    boolean existsByLeaderEmailAndEventId(String leaderEmail, int eventId);

    List<Registration> findByEventIdAndStatus(int eventId, String status);

}