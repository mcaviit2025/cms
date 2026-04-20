package com.example.cms.controller;

import com.example.cms.controller.dto.LoginRequest;
import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.AdminService;
import com.example.cms.service.EventService;
import com.example.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EventService eventService;



    // ========== LOGIN SYSTEM ==========

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String processLogin(@ModelAttribute LoginRequest loginRequest,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        boolean isValid = adminService.authenticate(loginRequest);

        if (isValid) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminEmail", loginRequest.getEmail());
            return "redirect:/admin/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ========== PROTECTED ROUTES (Add session check) ==========

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("events", eventService.getAllEvents());
        return "admin-dashboard";
    }

    @GetMapping("/admin/add-event")
    public String addEventPage(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        return "add-event";
    }

    @PostMapping("/admin/save-event")
    public String saveEvent(@ModelAttribute com.example.cms.entity.Event event, HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        eventService.saveEvent(event);
        return "redirect:/admin/add-event";
    }

    // Event Details - Show participants and statistics
    @Autowired
    RegistrationService registrationService;
    @GetMapping("/admin/event/{eventId}/details")
    public String eventDetails(@PathVariable int eventId, Model model, HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        // Get event details
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return "redirect:/admin/dashboard";
        }

        // Get all registrations for this event
        List<Registration> allRegistrations = registrationService.getRegistrationsByEventId(eventId);

        // Filter only APPROVED registrations for statistics
        List<Registration> approvedRegistrations = allRegistrations.stream()
                .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());

        // Calculate statistics
        int totalParticipants = allRegistrations.size();
        int approvedParticipants = approvedRegistrations.size();
        int codeSentCount = (int) approvedRegistrations.stream()
                .filter(r -> r.getParticipantCode() != null && !r.getParticipantCode().isEmpty())
                .count();

        // Calculate total collected money
        double totalCollected = approvedRegistrations.size() * event.getFee();

        // Add to model
        model.addAttribute("event", event);
        model.addAttribute("registrations", allRegistrations);
        model.addAttribute("totalParticipants", totalParticipants);
        model.addAttribute("approvedParticipants", approvedParticipants);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("codeSentCount", codeSentCount);

        return "event-details";
    }
}