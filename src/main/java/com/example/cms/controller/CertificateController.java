package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.CertificateEmailService;
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

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private CertificateEmailService certificateEmailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @GetMapping
    public String certificateManagement(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        return "admin/certificate-management";
    }

    @GetMapping("/event/{eventId}")
    @ResponseBody
    public List<Registration> getEventParticipants(@PathVariable Integer eventId) {
        List<Registration> registrations = registrationService.getRegistrationsByEventId(eventId);

        // Filter only APPROVED registrations for certificate page
        List<Registration> approvedRegistrations = registrations.stream()
                .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());

        // Clean up data to avoid JSON issues
        for (Registration reg : approvedRegistrations) {
            if (reg.getMemberNames() == null) reg.setMemberNames("");
            if (reg.getMemberEmails() == null) reg.setMemberEmails("");
            if (reg.getTeamName() == null) reg.setTeamName("");
            if (reg.getParticipantCode() == null) reg.setParticipantCode("");
            if (reg.getEmailStatus() == null) reg.setEmailStatus("NOT_SENT");
        }

        return approvedRegistrations;
    }

    @GetMapping("/download/{registrationId}")
    public void downloadCertificate(@PathVariable Integer registrationId,
                                    HttpServletResponse response,
                                    HttpSession session) throws Exception {
        if (session.getAttribute("adminLoggedIn") == null) {
            response.sendRedirect("/admin/login");
            return;
        }

        Registration registration = registrationService.getRegistrationById(registrationId);
        if (registration == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Registration not found");
            return;
        }

        Event event = eventService.getEventById(registration.getEventId());
        if (event == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found");
            return;
        }

        byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(registration, event, registration.getLeaderName());

        String fileName = "Certificate_" + registration.getLeaderName().replaceAll(" ", "_") + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @PostMapping("/send/{registrationId}")
    @ResponseBody
    public Map<String, Object> sendCertificateEmail(@PathVariable Integer registrationId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("adminLoggedIn") == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        try {
            Registration registration = registrationService.getRegistrationById(registrationId);
            if (registration == null) {
                response.put("success", false);
                response.put("error", "Registration not found");
                return response;
            }

            Event event = eventService.getEventById(registration.getEventId());
            if (event == null) {
                response.put("success", false);
                response.put("error", "Event not found");
                return response;
            }

            // Send email based on participation type
            if ("Group".equalsIgnoreCase(registration.getParticipationType())) {
                certificateEmailService.sendCertificateToGroup(registration, event);
            } else {
                certificateEmailService.sendCertificateToParticipant(registration, event,
                        registration.getLeaderName(), registration.getLeaderEmail());
            }

            // Update email status
            registration.setEmailStatus("SENT");
            registrationService.save(registration);

            response.put("success", true);
            response.put("message", "Certificate sent successfully");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/send-all/{eventId}")
    @ResponseBody
    public Map<String, Object> sendAllCertificates(@PathVariable Integer eventId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("adminLoggedIn") == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                response.put("success", false);
                response.put("error", "Event not found");
                return response;
            }

            List<Registration> registrations = registrationService.getRegistrationsByEventId(eventId);
            List<Registration> approvedRegistrations = registrations.stream()
                    .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                    .toList();

            int sentCount = 0;
            for (Registration reg : approvedRegistrations) {
                if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                    certificateEmailService.sendCertificateToGroup(reg, event);
                } else {
                    certificateEmailService.sendCertificateToParticipant(reg, event,
                            reg.getLeaderName(), reg.getLeaderEmail());
                }
                reg.setEmailStatus("SENT");
                registrationService.save(reg);
                sentCount++;
            }

            response.put("success", true);
            response.put("message", "Certificates sent to " + sentCount + " participants");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}