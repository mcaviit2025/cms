package com.example.cms.controller;

import com.example.cms.entity.EvaluationCriteria;
import com.example.cms.entity.Event;
import com.example.cms.repository.EvaluationCriteriaRepository;
import com.example.cms.repository.EventRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminEvaluationController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EvaluationCriteriaRepository criteriaRepository;

    @GetMapping("/admin/design-criteria")
    public String designCriteriaForm(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "admin/design-criteria";
    }

    @GetMapping("/admin/design-criteria/{eventId}")
    public String designCriteriaForEvent(@PathVariable Integer eventId, Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        List<EvaluationCriteria> criteria = criteriaRepository.findByEventIdOrderByDisplayOrderAsc(eventId);

        model.addAttribute("event", event);
        model.addAttribute("criteria", criteria);
        return "admin/criteria-builder";
    }

    @PostMapping("/admin/save-criteria")
    public String saveCriteria(@RequestParam Integer eventId,
                               @RequestParam List<String> criteriaNames,
                               @RequestParam List<Integer> maxMarks,
                               @RequestParam(required = false) List<String> descriptions,
                               RedirectAttributes redirectAttributes) {
        try {
            List<EvaluationCriteria> existing = criteriaRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
            criteriaRepository.deleteAll(existing);

            for (int i = 0; i < criteriaNames.size(); i++) {
                String name = criteriaNames.get(i);
                Integer maxMark = maxMarks.get(i);
                String desc = (descriptions != null && i < descriptions.size()) ? descriptions.get(i) : "";

                if (name != null && !name.trim().isEmpty() && maxMark != null && maxMark >= 1 && maxMark <= 100) {
                    EvaluationCriteria criteria = new EvaluationCriteria();
                    criteria.setEventId(eventId);
                    criteria.setCriteriaName(name.trim());
                    criteria.setMaxMarks(maxMark);
                    criteria.setDescription(desc);
                    criteria.setDisplayOrder(i);
                    criteria.setIsActive(true);
                    criteriaRepository.save(criteria);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Evaluation criteria saved successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save: " + e.getMessage());
        }

        return "redirect:/admin/design-criteria/" + eventId;
    }
}