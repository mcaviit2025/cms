package com.example.cms.controller;

import com.example.cms.entity.Payment;
import com.example.cms.entity.Registration;
import com.example.cms.repository.PaymentRepository;
import com.example.cms.service.EventService;
import com.example.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminRegistrationController {

    @Autowired
    private RegistrationService regService;

    @Autowired
    private EventService eventService;

    @Autowired
    private PaymentRepository paymentRepo;

    // LOAD PAGE
    @GetMapping("/registrations")
    public String registrationsHome(Model model){
        model.addAttribute("events", eventService.getAllEventsUnique());
        return "admin-registrations";
    }

    // FILTER
    @GetMapping("/registrations/{eventId}")
    public String getRegistrations(@PathVariable int eventId, Model model){

        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("selectedEvent", eventId);

        List<Registration> regs = regService.getByEvent(eventId);
        model.addAttribute("registrations", regs);




        Map<Integer, Payment> paymentMap = new HashMap<>();

        for(Registration r : regs){

            List<Payment> payments = paymentRepo.findAllByRegistrationId(r.getId());

            if(!payments.isEmpty()){
                paymentMap.put(r.getId(), payments.get(0)); // take first
            }
        }

        model.addAttribute("paymentMap", paymentMap);

        return "admin-registrations";
    }
    // APPROVE
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable int id){

        Registration reg = regService.getById(id);

        if(reg == null){
            return "redirect:/admin/registrations";
        }

        reg.setStatus("APPROVED");

        String code = "CMS-" + id;
        reg.setParticipantCode(code);

        regService.save(reg);

        return "redirect:/admin/registrations/" + reg.getEventId();
    }

    // REJECT (DELETE)
    @GetMapping("/reject/{id}")
    public String reject(@PathVariable int id){

        Registration reg = regService.getById(id);

        if(reg == null){
            return "redirect:/admin/registrations";
        }

        reg.setStatus("REJECTED");

        regService.save(reg);

        return "redirect:/admin/registrations/" + reg.getEventId();
    }
}