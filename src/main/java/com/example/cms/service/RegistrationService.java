package com.example.cms.service;

import com.example.cms.entity.Registration;

import java.util.List;

public interface RegistrationService {
    Registration save(Registration reg);
    Registration getById(int id);
    List<Registration> getByEvent(int eventId);
    void delete(int id);
    boolean exists(String email, int eventId);
    List<Registration> getApprovedByEvent(int eventId);
    List<Registration> getRegistrationsByEventId(int eventId);
    Registration getRegistrationById(int id);
}