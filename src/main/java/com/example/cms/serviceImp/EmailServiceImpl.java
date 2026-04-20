package com.example.cms.serviceImp;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.repository.RegistrationRepository;
import com.example.cms.service.EmailService;
import com.example.cms.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.CertificateService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.io.ByteArrayInputStream;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RegistrationRepository regRepo;

    @Autowired
    private EventService eventService;

    @Override
    public void sendCodesByEvent(int eventId) {

        Event event = eventService.getEventById(eventId);

        List<Registration> list =
                regRepo.findByEventIdAndStatus(eventId, "APPROVED");

        if(list.isEmpty()){
            throw new RuntimeException("No approved participants!");
        }

        for(Registration reg : list){

            try {

                // ❌ Skip if already sent
                if(reg.getEmailStatus() != null &&
                        reg.getEmailStatus().equalsIgnoreCase("SENT")){
                    continue;
                }

                // ✅ Generate code if not exists
                if(reg.getParticipantCode() == null){

                    String code = "CMS-E" + eventId + "-P" + reg.getId();
                    reg.setParticipantCode(code);
                }

                // ✅ Email content
                String message = "Dear " + reg.getLeaderName() + ",\n\n" +
                        "Your registration is approved.\n\n" +
                        "Event: " + event.getEventName() + "\n" +
                        "Date: " + event.getEventDate() + "\n" +
                        "Location: " + event.getVenue() + "\n\n" +
                        "Your Code: " + reg.getParticipantCode() + "\n\n" +
                        "Regards,\nCMS Team";

                // ✅ Send mail
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(reg.getLeaderEmail());
                mail.setSubject("CMS Fest Approval");
                mail.setText(message);

                mailSender.send(mail);

                // ✅ Mark as sent
                reg.setEmailStatus("SENT");
                regRepo.save(reg);

            } catch (Exception e){

                reg.setEmailStatus("NOT_SENT");
                regRepo.save(reg);

                e.printStackTrace();
            }
        }
    }

}