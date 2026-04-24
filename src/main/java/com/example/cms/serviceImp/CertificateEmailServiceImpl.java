package com.example.cms.serviceImp;

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

@Service
public class CertificateEmailServiceImpl implements CertificateEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DynamicCertificateService dynamicCertificateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendCertificateToParticipant(Registration registration, Event event, String memberName, String memberEmail) {
        try {
            System.out.println("1. Starting to send email to: " + memberEmail);

            // Generate PDF
            byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(registration, event, memberName);
            System.out.println("2. PDF generated, size: " + pdfBytes.length);

            // Create email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("Your Certificate for " + event.getEventName());
            helper.setText("Dear " + memberName + ",\n\nPlease find your certificate attached.\n\nBest regards,\nTechFest Team");
            helper.addAttachment("Certificate.pdf", new ByteArrayResource(pdfBytes));

            // Send
            mailSender.send(message);
            System.out.println("3. Email sent successfully to: " + memberEmail);

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email failed: " + e.getMessage());
        }
    }

    @Override
    public void sendCertificateToGroup(Registration registration, Event event) {
        sendCertificateToParticipant(registration, event, registration.getLeaderName(), registration.getLeaderEmail());

        if (registration.getMemberNames() != null && !registration.getMemberNames().isEmpty()) {
            String[] memberNames = registration.getMemberNames().split(",");
            String[] memberEmails = registration.getMemberEmails() != null ?
                    registration.getMemberEmails().split(",") : new String[0];

            for (int i = 0; i < memberNames.length; i++) {
                String name = memberNames[i].trim();
                String email = (i < memberEmails.length) ? memberEmails[i].trim() : null;
                if (email != null && !email.isEmpty()) {
                    sendCertificateToParticipant(registration, event, name, email);
                }
            }
        }
    }
}