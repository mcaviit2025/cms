package com.example.cms.serviceimpl;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.CertificateEmailService;
import com.example.cms.service.DynamicCertificateService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class CertificateEmailServiceImpl implements CertificateEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DynamicCertificateService dynamicCertificateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendCertificateToParticipant(Registration registration, Event event,
                                             String memberName, String memberEmail) {
        try {
            // Generate PDF certificate for this specific member
            byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(registration, event, memberName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("🎓 Your Certificate for " + event.getEventName() + " - Brainwave 2026");

            String eventDateStr = "TBD";
            if (event.getEventDate() != null && !event.getEventDate().isEmpty()) {
                eventDateStr = event.getEventDate();
            }

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Congratulations on your successful participation in %s at Brainwave 2026!\n\n" +
                            "Please find attached your participation certificate.\n\n" +
                            "Event: %s\n" +
                            "Date: %s\n\n" +
                            "Best regards,\n" +
                            "Brainwave 2026 Team\n" +
                            "Vidya Pratishthan's Institute of Information Technology, Baramati",
                    memberName,
                    event.getEventName(),
                    event.getEventName(),
                    eventDateStr
            );

            helper.setText(emailBody);
            helper.addAttachment("Certificate_Brainwave2026_" + memberName.replaceAll(" ", "_") + ".pdf",
                    new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("✅ Certificate sent to: " + memberEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send certificate to " + memberEmail + ": " + e.getMessage());
        }
    }

    @Override
    public void sendCertificateToGroup(Registration registration, Event event) {
        System.out.println("=== Sending certificates to GROUP ===");
        System.out.println("Team Name: " + registration.getTeamName());
        System.out.println("Leader: " + registration.getLeaderName() + " (" + registration.getLeaderEmail() + ")");

        // Send to leader with leader's name
        sendCertificateToParticipant(registration, event, registration.getLeaderName(), registration.getLeaderEmail());

        // Send to all team members with their own names
        if (registration.getMemberNames() != null && !registration.getMemberNames().isEmpty()) {
            String[] memberNames = registration.getMemberNames().split(",");
            String[] memberEmails = registration.getMemberEmails() != null ?
                    registration.getMemberEmails().split(",") : new String[0];

            for (int i = 0; i < memberNames.length; i++) {
                String name = memberNames[i].trim();
                String email = (i < memberEmails.length) ? memberEmails[i].trim() : null;

                if (email != null && !email.isEmpty()) {
                    System.out.println("  Sending to member: " + name + " (" + email + ")");
                    sendCertificateToParticipant(registration, event, name, email);
                }
            }
        }
        System.out.println("=== All certificates sent for team: " + registration.getTeamName() + " ===");
    }
}