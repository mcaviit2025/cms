package com.example.cms.controller;

import com.example.cms.dto.AssignJudgeDTO;
import com.example.cms.dto.JudgeDTO;
import com.example.cms.entity.Event;
import com.example.cms.entity.Judge;
import com.example.cms.service.EventService;
import com.example.cms.service.JudgeService;
import com.example.cms.repository.EventJudgeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminJudgeController {

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventJudgeRepository eventJudgeRepository;

    // ========== JUDGE MANAGEMENT ==========

    @GetMapping("/admin/judges")
    public String manageJudges(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        try {
            List<Judge> judges = judgeService.getAllJudges();
            model.addAttribute("judges", judges);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading judges: " + e.getMessage());
        }
        return "admin/manage-judges";
    }

    @GetMapping("/admin/judges/add")
    public String addJudgeForm(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("judge", new JudgeDTO());
        return "admin/add-judge";
    }

    @PostMapping("/admin/judges/add")
    public String addJudge(@ModelAttribute JudgeDTO judgeDTO, RedirectAttributes redirectAttributes) {
        try {
            judgeService.createJudge(judgeDTO);
            redirectAttributes.addFlashAttribute("success", "Judge created successfully! Username: " + judgeDTO.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create judge: " + e.getMessage());
        }
        return "redirect:/admin/judges";
    }

    @GetMapping("/admin/judges/edit/{id}")
    public String editJudgeForm(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        try {
            Judge judge = judgeService.getJudgeById(id);
            JudgeDTO judgeDTO = new JudgeDTO();
            judgeDTO.setId(judge.getId());
            judgeDTO.setUsername(judge.getUsername());
            judgeDTO.setFullName(judge.getFullName());
            judgeDTO.setIsActive(judge.getIsActive());
            model.addAttribute("judge", judgeDTO);
        } catch (Exception e) {
            return "redirect:/admin/judges";
        }
        return "admin/edit-judge";
    }

    @PostMapping("/admin/judges/edit/{id}")
    public String updateJudge(@PathVariable Integer id, @ModelAttribute JudgeDTO judgeDTO, RedirectAttributes redirectAttributes) {
        try {
            judgeService.updateJudge(id, judgeDTO);
            redirectAttributes.addFlashAttribute("success", "Judge updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update judge: " + e.getMessage());
        }
        return "redirect:/admin/judges";
    }

    @GetMapping("/admin/judges/toggle/{id}")
    public String toggleJudgeStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean isActive = judgeService.toggleJudgeStatus(id);
            redirectAttributes.addFlashAttribute("success", "Judge " + (isActive ? "activated" : "deactivated") + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to toggle status: " + e.getMessage());
        }
        return "redirect:/admin/judges";
    }

    @GetMapping("/admin/judges/reset-password/{id}")
    public String resetPassword(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = "judge" + id;
            judgeService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset to: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reset password: " + e.getMessage());
        }
        return "redirect:/admin/judges";
    }

    @GetMapping("/admin/judges/delete/{id}")
    public String deleteJudge(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            judgeService.deleteJudge(id);
            redirectAttributes.addFlashAttribute("success", "Judge deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete judge: " + e.getMessage());
        }
        return "redirect:/admin/judges";
    }

    // ========== ASSIGN JUDGE TO EVENT ==========

    @GetMapping("/admin/assign-judge")
    public String assignJudgeForm(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        try {
            List<Event> events = eventService.getAllEvents();
            List<Judge> judges = judgeService.getAllJudges();

            // Get events with assigned judges
            List<Event> eventsWithAssignments = new ArrayList<>();
            if (events != null) {
                for (Event event : events) {
                    List<Integer> judgeIds = eventJudgeRepository.findJudgeIdsByEventId(event.getEventId());
                    List<Judge> assignedJudges = new ArrayList<>();
                    if (judgeIds != null && !judgeIds.isEmpty()) {
                        for (Integer judgeId : judgeIds) {
                            try {
                                Judge judge = judgeService.getJudgeById(judgeId);
                                if (judge != null) {
                                    assignedJudges.add(judge);
                                }
                            } catch (Exception e) {
                                System.out.println("Error getting judge: " + e.getMessage());
                            }
                        }
                    }
                    event.setAssignedJudges(assignedJudges);
                    eventsWithAssignments.add(event);
                }
            }

            model.addAttribute("events", events);
            model.addAttribute("judges", judges);
            model.addAttribute("eventsWithAssignments", eventsWithAssignments);
            model.addAttribute("assignment", new AssignJudgeDTO());

        } catch (Exception e) {
            System.out.println("Error loading assign form: " + e.getMessage());
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }
        return "admin/assign-judge";
    }

    // KEEP ONLY THIS ONE - The method with @RequestParam
    @PostMapping("/admin/assign-judge")
    public String assignJudge(@RequestParam("eventId") Integer eventId,
                              @RequestParam("judgeId") Integer judgeId,
                              RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (eventId == null || judgeId == null) {
                redirectAttributes.addFlashAttribute("error", "Please select both event and judge");
                return "redirect:/admin/assign-judge";
            }

            // Check if already assigned
            boolean alreadyAssigned = eventJudgeRepository.existsByEventIdAndJudgeId(eventId, judgeId);
            if (alreadyAssigned) {
                Event event = eventService.getEventById(eventId);
                Judge judge = judgeService.getJudgeById(judgeId);
                redirectAttributes.addFlashAttribute("error",
                        "Judge '" + judge.getFullName() + "' is already assigned to event '" + event.getEventName() + "'");
                return "redirect:/admin/assign-judge";
            }

            // Assign judge to event
            AssignJudgeDTO assignDTO = new AssignJudgeDTO();
            assignDTO.setEventId(eventId);
            assignDTO.setJudgeId(judgeId);
            judgeService.assignJudgeToEvent(assignDTO);

            Event event = eventService.getEventById(eventId);
            Judge judge = judgeService.getJudgeById(judgeId);

            redirectAttributes.addFlashAttribute("success",
                    "Judge '" + judge.getFullName() + "' successfully assigned to event '" + event.getEventName() + "'");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign judge: " + e.getMessage());
        }
        return "redirect:/admin/assign-judge";
    }

    @GetMapping("/admin/remove-judge/{eventId}/{judgeId}")
    public String removeJudge(@PathVariable Integer eventId, @PathVariable Integer judgeId, RedirectAttributes redirectAttributes) {
        try {
            judgeService.removeJudgeFromEvent(eventId, judgeId);
            redirectAttributes.addFlashAttribute("success", "Judge removed from event successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove judge: " + e.getMessage());
        }
        return "redirect:/admin/assign-judge";
    }
}