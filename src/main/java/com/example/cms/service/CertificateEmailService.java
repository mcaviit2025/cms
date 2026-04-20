package com.example.cms.service;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;

public interface CertificateEmailService {

    void sendCertificateToParticipant(Registration registration, Event event, String memberName, String memberEmail);

    void sendCertificateToGroup(Registration registration, Event event);
}