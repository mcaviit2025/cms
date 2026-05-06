package com.example.cms.controller;

import com.example.cms.dto.ExportConfigDTO;
import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.entity.Score;
import com.example.cms.service.EventService;
import com.example.cms.service.ExportService;
import com.example.cms.service.RegistrationService;
import com.example.cms.service.ScoreService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EventManagementController {

    @Autowired
    private EventService eventService;

    // ========== EVENT LIST PAGE ==========
    @GetMapping("/admin/events")
    public String listEvents(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        return "admin/events";
    }

    // ========== CREATE EVENT FORM PAGE ==========
    @GetMapping("/admin/events/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("event", new Event());
        return "admin/add-event";
    }

    // ========== SAVE NEW EVENT ==========
    @PostMapping("/admin/events/create")
    public String createEvent(@RequestParam String eventName,
                              @RequestParam String description,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                              @RequestParam String venue,
                              @RequestParam double fee,
                              @RequestParam String category,
                              @RequestParam String participationType,
                              @RequestParam int maxParticipants,
                              @RequestParam(required = false) Integer groupSize,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {

        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        try {
            // Validate date
            if (eventDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Event date cannot be in the past!");
                return "redirect:/admin/events/create";
            }

            Event event = new Event();
            event.setEventName(eventName);
            event.setDescription(description);
            event.setEventDate(eventDate);
            event.setVenue(venue);
            event.setFee(fee);
            event.setCategory(category);
            event.setParticipationType(participationType);
            event.setMaxParticipants(maxParticipants);
            event.setGroupSize(groupSize);
            event.setActive(true);

            eventService.saveEvent(event);

            redirectAttributes.addFlashAttribute("success", "Event created successfully!");
            return "redirect:/admin/events";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
            return "redirect:/admin/events/create";
        }
    }

     //========== EDIT EVENT FORM PAGE ==========
    @GetMapping("/admin/events/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        Event event = eventService.getEventById(id);
        if (event == null) {
            return "redirect:/admin/events";
        }
        model.addAttribute("event", event);
        return "admin/add-event";
    }

    // ========== UPDATE EVENT ==========
    @PostMapping("/admin/events/edit/{id}")
    public String updateEvent(@PathVariable int id,
                              @RequestParam String eventName,
                              @RequestParam String description,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                              @RequestParam String venue,
                              @RequestParam double fee,
                              @RequestParam String category,
                              @RequestParam String participationType,
                              @RequestParam int maxParticipants,
                              @RequestParam(required = false) Integer groupSize,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {

        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        try {
            Event existingEvent = eventService.getEventById(id);
            if (existingEvent == null) {
                redirectAttributes.addFlashAttribute("error", "Event not found!");
                return "redirect:/admin/events";
            }

            existingEvent.setEventName(eventName);
            existingEvent.setDescription(description);
            existingEvent.setEventDate(eventDate);
            existingEvent.setVenue(venue);
            existingEvent.setFee(fee);
            existingEvent.setCategory(category);
            existingEvent.setParticipationType(participationType);
            existingEvent.setMaxParticipants(maxParticipants);
            existingEvent.setGroupSize(groupSize);

            eventService.saveEvent(existingEvent);

            redirectAttributes.addFlashAttribute("success", "Event updated successfully!");
            return "redirect:/admin/events";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update event: " + e.getMessage());
            return "redirect:/admin/events/edit/" + id;
        }
    }

    // DELETE method for AJAX call from frontend
    @DeleteMapping("/admin/events/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteEvent(@PathVariable int id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("adminLoggedIn") == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        try {
            eventService.deleteEvent(id);
            response.put("success", true);
            response.put("message", "Event deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // GET method for form submission (if needed)
    @GetMapping("/admin/events/delete-confirm/{id}")
    public String deleteEventConfirm(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("success", "Event deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }


    @Autowired
    RegistrationService registrationService;
    @Autowired
    ScoreService scoreService;

    @GetMapping("/admin/events/report/{id}")
    @ResponseBody
    public Map<String, Object> getEventReport(@PathVariable int id, HttpSession session) {
        Map<String, Object> report = new HashMap<>();

        if (session.getAttribute("adminLoggedIn") == null) {
            report.put("error", "Not logged in");
            return report;
        }

        try {
            Event event = eventService.getEventById(id);
            if (event == null) {
                report.put("error", "Event not found");
                return report;
            }

            List<Registration> registrations = registrationService.getRegistrationsByEventId(id);
            List<Score> scores = scoreService.getScoresByEventId(id);

            long approvedCount = registrations.stream()
                    .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                    .count();

            long pendingCount = registrations.stream()
                    .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                    .count();

            // Calculate total collected amount
            double totalCollected = 0;
            for (Registration reg : registrations) {
                if ("APPROVED".equalsIgnoreCase(reg.getStatus())) {
                    if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                        int memberCount = 1;
                        if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                            memberCount += reg.getMemberNames().split(",").length;
                        }
                        totalCollected += event.getFee() * memberCount;
                    } else {
                        totalCollected += event.getFee();
                    }
                }
            }

            // Calculate average and top score
            double avgScore = scores.stream()
                    .mapToInt(Score::getTotalScore)
                    .average()
                    .orElse(0);

            int topScore = scores.stream()
                    .mapToInt(Score::getTotalScore)
                    .max()
                    .orElse(0);

            report.put("eventName", event.getEventName());
            report.put("totalParticipants", registrations.size());
            report.put("approvedParticipants", approvedCount);
            report.put("pendingParticipants", pendingCount);
            report.put("totalCollected", totalCollected);
            report.put("avgScore", Math.round(avgScore * 10) / 10.0);
            report.put("topScore", topScore);

        } catch (Exception e) {
            e.printStackTrace();
            report.put("error", e.getMessage());
        }

        return report;
    }

    // Export participants (GET method for direct download)
    @Autowired
    ExportService exportService;
    @GetMapping("/admin/events/export/{eventId}")
    public void exportParticipants(@PathVariable int eventId,
                                   @RequestParam(defaultValue = "EXCEL") String format,
                                   @RequestParam(defaultValue = "ALL") String status,
                                   @RequestParam(defaultValue = "true") boolean includeScores,
                                   HttpServletResponse response,
                                   HttpSession session) throws IOException {

        if (session.getAttribute("adminLoggedIn") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found");
            return;
        }

        try {
            ExportConfigDTO config = new ExportConfigDTO();
            config.setEventId(eventId);
            config.setEventName(event.getEventName());
            config.setFileFormat(format);
            config.setParticipantStatus(status);
            config.setIncludeScores(includeScores);

            byte[] exportData = exportService.exportParticipants(config);

            String filename = event.getEventName().replaceAll(" ", "_") + "_Participants." +
                    ("CSV".equals(format) ? "csv" : "xlsx");

            String contentType = "CSV".equals(format) ?
                    "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(exportData.length);
            response.getOutputStream().write(exportData);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/events/export/custom")
    @ResponseBody
    public void exportParticipantsCustom(@RequestBody ExportConfigDTO config,
                                         HttpServletResponse response,
                                         HttpSession session) throws IOException {

        if (session.getAttribute("adminLoggedIn") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        byte[] exportData = exportService.exportParticipants(config);

        String filename = config.getEventName().replaceAll(" ", "_") + "_Participants." +
                ("CSV".equals(config.getFileFormat()) ? "csv" : "xlsx");

        String contentType = "CSV".equals(config.getFileFormat()) ?
                "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(exportData);
        response.getOutputStream().flush();
    }
}