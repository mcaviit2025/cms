package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.Payment;
import com.example.cms.entity.Registration;
import com.example.cms.repository.PaymentRepository;
import com.example.cms.service.EventService;
import com.example.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminRegistrationController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PaymentRepository paymentRepository;

    // LOAD PAGE - FIXED: Change getAllEventsUnique() to getAllEvents()
    @GetMapping("/admin/registrations")
    public String registrationsHome(Model model) {
        model.addAttribute("events", eventService.getAllEvents());  // ← FIXED
        return "admin-registrations";
    }

    @GetMapping("/admin/registrations/{eventId}")
    public String getRegistrations(@PathVariable int eventId, Model model) {
        model.addAttribute("events", eventService.getAllEvents());  // ← FIXED
        model.addAttribute("selectedEvent", eventId);

        List<Registration> registrations = registrationService.getRegistrationsByEventId(eventId);

        // Get payment proofs for each registration
        Map<Integer, Payment> paymentMap = new HashMap<>();
        for (Registration reg : registrations) {
            paymentRepository.findByRegistrationId(reg.getId()).ifPresent(payment ->
                    paymentMap.put(reg.getId(), payment)
            );
        }

        model.addAttribute("registrations", registrations);
        model.addAttribute("paymentMap", paymentMap);

        return "admin-registrations";
    }

    @GetMapping("/admin/approve/{id}")
    public String approveRegistration(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Registration registration = registrationService.getRegistrationById(id);
            if (registration != null) {
                registration.setStatus("APPROVED");
                registrationService.save(registration);
                redirectAttributes.addFlashAttribute("success", "Registration approved successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve: " + e.getMessage());
        }
        return "redirect:/admin/registrations/" + registrationService.getRegistrationById(id).getEventId();
    }

    @GetMapping("/admin/reject/{id}")
    public String rejectRegistration(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Registration registration = registrationService.getRegistrationById(id);
            if (registration != null) {
                registration.setStatus("REJECTED");
                registrationService.save(registration);
                redirectAttributes.addFlashAttribute("success", "Registration rejected!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject: " + e.getMessage());
        }
        return "redirect:/admin/registrations/" + registrationService.getRegistrationById(id).getEventId();
    }
}