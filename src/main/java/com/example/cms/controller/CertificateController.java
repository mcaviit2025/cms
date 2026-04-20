package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.CertificateEmailService;
import com.example.cms.service.DynamicCertificateService;
import com.example.cms.service.EventService;
import com.example.cms.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    private CertificateEmailService certificateEmailService;

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
        return registrationService.getRegistrationsByEventId(eventId);
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

        // Generate certificate for leader with leader's name
        byte[] pdfBytes = dynamicCertificateService.generateCertificatePdf(registration, event, registration.getLeaderName());

        String fileName = "Certificate_Brainwave2026_" + registration.getLeaderName().replaceAll(" ", "_") + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @PostMapping("/send/{registrationId}")
    public String sendCertificateEmail(@PathVariable Integer registrationId,
                                       RedirectAttributes redirectAttributes,
                                       HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        try {
            Registration registration = registrationService.getRegistrationById(registrationId);
            if (registration == null) {
                redirectAttributes.addFlashAttribute("error", "Registration not found");
                return "redirect:/admin/certificates";
            }

            Event event = eventService.getEventById(registration.getEventId());
            if (event == null) {
                redirectAttributes.addFlashAttribute("error", "Event not found");
                return "redirect:/admin/certificates";
            }

            if ("Group".equalsIgnoreCase(registration.getParticipationType())) {
                certificateEmailService.sendCertificateToGroup(registration, event);
                int memberCount = getMemberCount(registration);
                redirectAttributes.addFlashAttribute("success", "✅ Certificates sent successfully to " + memberCount + " team members!");
            } else {
                certificateEmailService.sendCertificateToParticipant(registration, event,
                        registration.getLeaderName(), registration.getLeaderEmail());
                redirectAttributes.addFlashAttribute("success", "✅ Certificate sent successfully to " + registration.getLeaderName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to send: " + e.getMessage());
        }

        return "redirect:/admin/certificates";
    }

    @PostMapping("/send-all/{eventId}")
    public String sendAllCertificates(@PathVariable Integer eventId,
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                redirectAttributes.addFlashAttribute("error", "Event not found");
                return "redirect:/admin/certificates";
            }

            List<Registration> registrations = registrationService.getRegistrationsByEventId(eventId);
            List<Registration> approvedRegistrations = registrations.stream()
                    .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                    .toList();

            int sentCount = 0;
            for (Registration reg : approvedRegistrations) {
                if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                    certificateEmailService.sendCertificateToGroup(reg, event);
                    sentCount += getMemberCount(reg);
                } else {
                    certificateEmailService.sendCertificateToParticipant(reg, event,
                            reg.getLeaderName(), reg.getLeaderEmail());
                    sentCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success", "✅ Certificates sent successfully to " + sentCount + " participants!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send: " + e.getMessage());
        }

        return "redirect:/admin/certificates";
    }

    private int getMemberCount(Registration registration) {
        int count = 1; // Leader
        if (registration.getMemberNames() != null && !registration.getMemberNames().isEmpty()) {
            count += registration.getMemberNames().split(",").length;
        }
        return count;
    }
}