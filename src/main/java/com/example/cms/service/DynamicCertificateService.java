package com.example.cms.service;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;

public interface DynamicCertificateService {

    byte[] generateCertificatePdf(Registration registration, Event event, String memberName);

    String generateCertificateHtml(Registration registration, Event event, String memberName);
}