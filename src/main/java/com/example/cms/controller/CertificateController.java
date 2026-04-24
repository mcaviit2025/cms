package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.DynamicCertificateService;
import com.example.cms.service.EventService;
import com.example.cms.service.RegistrationService;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/certificates")
public class CertificateController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private DynamicCertificateService dynamicCertificateService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ─────────────────────────────────────────────────────────────────────────
    // Page
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    public String certificateManagement(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/certificate-management";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // List participants for an event
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/event/{eventId}")
    @ResponseBody
    public List<Registration> getEventParticipants(@PathVariable Integer eventId) {
        return registrationService.getRegistrationsByEventId(eventId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Download certificate as PDF
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/download/{registrationId}")
    public void downloadCertificate(@PathVariable Integer registrationId,
                                    HttpServletResponse response,
                                    HttpSession session) throws Exception {
        if (session.getAttribute("adminLoggedIn") == null) {
            response.sendRedirect("/admin/login");
            return;
        }

        Registration registration = registrationService.getRegistrationById(registrationId);
        Event event = eventService.getEventById(registration.getEventId());

        byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(
                registration, event, registration.getLeaderName());

        String fileName = "Certificate_" + registration.getLeaderName().replaceAll("\\s+", "_") + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setContentType("application/pdf");
        response.setContentLength(pdfBytes.length);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send single certificate via email
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/send/{registrationId}")
    @ResponseBody
    public Map<String, Object> sendCertificateEmail(@PathVariable Integer registrationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Registration registration = registrationService.getRegistrationById(registrationId);

            // ✅ FIX: Validate email before attempting to send
            String toEmail = registration.getLeaderEmail();
            if (toEmail == null || toEmail.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "No email address found for participant: " + registration.getLeaderName());
                return result;
            }

            Event event = eventService.getEventById(registration.getEventId());

            // 1. Generate PDF
            byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(
                    registration, event, registration.getLeaderName());

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new Exception("PDF generation failed (0 bytes).");
            }

            // 2. Send email with PDF attachment
            sendEmail(toEmail.trim(),
                    registration.getLeaderName(),
                    event.getEventName(),
                    pdfBytes);

            // 3. Mark as SENT in database
            registration.setEmailStatus("SENT");
            registrationService.save(registration);

            result.put("success", true);
            result.put("message", "Certificate successfully sent to " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Failed to send email: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ✅ NEW: Send all pending certificates for an event
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/send-all/{eventId}")
    @ResponseBody
    public Map<String, Object> sendAllCertificates(@PathVariable Integer eventId) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();

        try {
            List<Registration> registrations = registrationService.getRegistrationsByEventId(eventId);
            Event event = eventService.getEventById(eventId);

            // Only process APPROVED registrations not yet sent
            List<Registration> pending = registrations.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()) && !"SENT".equals(r.getEmailStatus()))
                    .toList();

            if (pending.isEmpty()) {
                result.put("success", true);
                result.put("message", "No pending certificates to send.");
                return result;
            }

            for (Registration registration : pending) {
                try {
                    String toEmail = registration.getLeaderEmail();
                    if (toEmail == null || toEmail.trim().isEmpty()) {
                        failCount++;
                        errors.append("No email for ").append(registration.getLeaderName()).append(". ");
                        continue;
                    }

                    byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(
                            registration, event, registration.getLeaderName());

                    if (pdfBytes == null || pdfBytes.length == 0) {
                        failCount++;
                        errors.append("PDF empty for ").append(registration.getLeaderName()).append(". ");
                        continue;
                    }

                    sendEmail(toEmail.trim(), registration.getLeaderName(), event.getEventName(), pdfBytes);

                    registration.setEmailStatus("SENT");
                    registrationService.save(registration);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    errors.append(registration.getLeaderName()).append(": ").append(e.getMessage()).append(". ");
                }
            }

            result.put("success", failCount == 0);
            result.put("message", successCount + " certificate(s) sent successfully."
                    + (failCount > 0 ? " " + failCount + " failed: " + errors : ""));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal: send email with PDF attachment
    // ─────────────────────────────────────────────────────────────────────────

    private void sendEmail(String toEmail, String recipientName, String eventName, byte[] pdfBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        // true = multipart (required for attachments)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("🎓 Certificate: " + eventName);

        String body = "<h3>Hello " + recipientName + ",</h3>"
                + "<p>Please find your participation certificate for <b>" + eventName + "</b> attached.</p>"
                + "<p>Best Regards,<br/>TechFest Team</p>";

        helper.setText(body, true);

        // ✅ FIX: Custom ByteArrayResource that returns a proper filename.
        // Spring's default ByteArrayResource.getFilename() returns null,
        // which causes many SMTP servers to reject or strip the attachment.
        final String fileName = "Certificate_" + recipientName.replaceAll("\\s+", "_") + ".pdf";
        ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        helper.addAttachment(fileName, pdfResource, "application/pdf");
        mailSender.send(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SMTP connectivity test
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/test-mail")
    @ResponseBody
    public String testMail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(fromEmail);
            message.setSubject("CMS SMTP Test");
            message.setText("If you see this, your email configuration is correct.");
            message.setFrom(fromEmail);
            mailSender.send(message);
            return "✅ Test email sent to " + fromEmail;
        } catch (Exception e) {
            return "❌ Test failed: " + e.getMessage();
        }
    }
}