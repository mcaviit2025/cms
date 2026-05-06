package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.EventService;
import com.example.cms.service.JudgeService;
import com.example.cms.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private JudgeService judgeService;

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Your existing login logic
        if ("admin@cms.com".equals(email) && "admin123".equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            return "redirect:/admin/dashboard";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid email or password");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        // Get all events for the dashboard
        List<Event> events = eventService.getAllEvents();

        // Calculate statistics for each event
        for (Event event : events) {
            List<Registration> registrations = registrationService.getRegistrationsByEventId(event.getEventId());
            event.setTotalParticipants(registrations.size());
        }

        // Calculate dashboard statistics
        long totalEvents = eventService.getTotalEventCount();
        int totalParticipants = eventService.getTotalParticipantsCount();
        double totalRevenue = eventService.getTotalRevenue();
//        long pendingApprovals = registrationService.getPendingApprovalsCount();

        model.addAttribute("events", events);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalParticipants", totalParticipants);
        model.addAttribute("totalRevenue", totalRevenue);
//        model.addAttribute("pendingApprovals", pendingApprovals);

        return "admin-dashboard";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }


}