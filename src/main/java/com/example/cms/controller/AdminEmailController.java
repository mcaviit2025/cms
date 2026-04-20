package com.example.cms.controller;

import com.example.cms.service.EmailService;
import com.example.cms.service.EventService;

import com.example.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminEmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EventService eventService;

    @Autowired
    RegistrationService registrationService;

    // PAGE
    @GetMapping("/send-email/{eventId}")
    public String emailPage(@PathVariable int eventId, Model model){

        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("selectedEvent", eventId);

        model.addAttribute("registrations",
                registrationService.getApprovedByEvent(eventId));

        return "admin-email";
    }

    @GetMapping("/send-email")
    public String emailPage(Model model){

        model.addAttribute("events", eventService.getAllEvents());

        return "admin-email";
    }

    // ACTION
    @PostMapping("/send-codes")
    public String sendCodes(@RequestParam int eventId){

        emailService.sendCodesByEvent(eventId);

        return "redirect:/admin/send-email/" + eventId;
    }
}