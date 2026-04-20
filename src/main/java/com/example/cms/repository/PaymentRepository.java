package com.example.cms.repository;

import com.example.cms.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findAllByRegistrationId(int registrationId);

    void deleteByRegistrationId(int registrationId);
}