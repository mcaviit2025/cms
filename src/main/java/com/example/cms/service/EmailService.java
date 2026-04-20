package com.example.cms.service;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;

public interface EmailService {

    void sendCodesByEvent(int eventId);

    // Add these new methods for certificate sending

}