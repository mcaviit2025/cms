package com.example.cms.service;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;

import java.io.ByteArrayInputStream;

public interface CertificateService {

    ByteArrayInputStream generateCertificate(Registration registration, Event event, String memberName);
}