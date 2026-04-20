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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

@Controller
public class PageController {

    @Autowired
    private EventService service;

    @Autowired
    private RegistrationService regService;

    @Autowired
    PaymentRepository paymentrepo;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("events", service.getAllEvents());
        return "landing";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/register/{id}")
    public String registerPage(@PathVariable int id, Model model) {
        model.addAttribute("event", service.getEventById(id));
        return "register";
    }

    @PostMapping("/register/save")
    public String saveRegistration(
            @ModelAttribute Registration reg,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String memberNames,
            @RequestParam(required = false) String memberEmails,
            @RequestParam(required = false) String userParticipationType,
            RedirectAttributes ra
    ) {
        try {
            Event event = service.getEventById(reg.getEventId());

            // Set participation type from user selection (for Both events)
            if (userParticipationType != null && !userParticipationType.isEmpty()) {
                reg.setParticipationType(userParticipationType);
            }

            // ==================== BASIC VALIDATIONS ====================

            // Validate leader name
            if (reg.getLeaderName() == null || reg.getLeaderName().trim().length() < 2) {
                ra.addFlashAttribute("error", "Leader name must be at least 2 characters");
                return "redirect:/register/" + reg.getEventId();
            }

            // Validate leader email
            if (reg.getLeaderEmail() == null || !EMAIL_PATTERN.matcher(reg.getLeaderEmail()).matches()) {
                ra.addFlashAttribute("error", "Enter a valid email address for leader");
                return "redirect:/register/" + reg.getEventId();
            }

            // Validate mobile number
            if (reg.getPhone() == null || !MOBILE_PATTERN.matcher(reg.getPhone()).matches()) {
                ra.addFlashAttribute("error", "Enter a valid 10-digit mobile number starting with 6,7,8,9");
                return "redirect:/register/" + reg.getEventId();
            }

            // Validate college name
            if (reg.getCollege() == null || reg.getCollege().trim().length() < 3) {
                ra.addFlashAttribute("error", "College name must be at least 3 characters");
                return "redirect:/register/" + reg.getEventId();
            }

            // Validate UTR number
            if (reg.getUtrNumber() == null || reg.getUtrNumber().trim().length() < 5) {
                ra.addFlashAttribute("error", "Enter a valid UTR number (minimum 5 characters)");
                return "redirect:/register/" + reg.getEventId();
            }

            // ==================== COLLECT ALL EMAILS IN THIS REGISTRATION ====================

            Set<String> allEmailsInThisRegistration = new HashSet<>();
            allEmailsInThisRegistration.add(reg.getLeaderEmail().trim().toLowerCase());

            // For group events, collect member emails
            if ("Group".equalsIgnoreCase(reg.getParticipationType()) && memberEmails != null && !memberEmails.isEmpty()) {
                String[] memberEmailArray = memberEmails.split(",");
                for (String email : memberEmailArray) {
                    if (email != null && !email.trim().isEmpty()) {
                        allEmailsInThisRegistration.add(email.trim().toLowerCase());
                    }
                }
            }

            // ==================== CHECK FOR DUPLICATES WITHIN SAME REGISTRATION ====================

            if (allEmailsInThisRegistration.size() != (1 + (memberEmails != null && !memberEmails.isEmpty() ? memberEmails.split(",").length : 0))) {
                ra.addFlashAttribute("error", "Duplicate email addresses found within the same registration! Each member must have a unique email.");
                return "redirect:/register/" + reg.getEventId();
            }

            // ==================== CHECK IF ANY PERSON IS ALREADY REGISTERED IN THIS EVENT ====================

            // Get all existing registrations for this event
            List<Registration> existingRegistrations = regService.getRegistrationsByEventId(reg.getEventId());

            // Collect all emails already registered in this event (as leader or member)
            Set<String> registeredEmails = new HashSet<>();
            for (Registration existingReg : existingRegistrations) {
                // Add leader email
                if (existingReg.getLeaderEmail() != null) {
                    registeredEmails.add(existingReg.getLeaderEmail().trim().toLowerCase());
                }
                // Add member emails
                if (existingReg.getMemberEmails() != null && !existingReg.getMemberEmails().isEmpty()) {
                    String[] memberEmailArray = existingReg.getMemberEmails().split(",");
                    for (String email : memberEmailArray) {
                        if (email != null && !email.trim().isEmpty()) {
                            registeredEmails.add(email.trim().toLowerCase());
                        }
                    }
                }
            }

            // Check if any email in this registration is already registered
            for (String email : allEmailsInThisRegistration) {
                if (registeredEmails.contains(email)) {
                    ra.addFlashAttribute("error", "The email " + email + " is already registered for this event! Each person can register only once.");
                    return "redirect:/register/" + reg.getEventId();
                }
            }

            // ==================== VALIDATE TEAM SIZE FOR GROUP EVENTS ====================

            if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                int maxTeamSize = 4;  // Max 4 members total (Leader + up to 3 members)

                // Validate team name
                if (reg.getTeamName() == null || reg.getTeamName().trim().length() < 2) {
                    ra.addFlashAttribute("error", "Team name must be at least 2 characters");
                    return "redirect:/register/" + reg.getEventId();
                }

                // Count current members (excluding leader)
                int memberCount = 0;
                String[] names = {};
                String[] emails = {};

                if (memberNames != null && !memberNames.isEmpty()) {
                    names = memberNames.split(",");
                    emails = memberEmails != null ? memberEmails.split(",") : new String[0];
                    memberCount = names.length;
                }

                int totalTeamSize = 1 + memberCount;  // Leader + members

                // Validate team size (1 to 4 total members)
                if (totalTeamSize < 1 || totalTeamSize > maxTeamSize) {
                    ra.addFlashAttribute("error", "Team size must be between 1 and " + maxTeamSize + " members (including leader)");
                    return "redirect:/register/" + reg.getEventId();
                }

                // Validate each member's name and email (if members exist)
                for (int i = 0; i < names.length; i++) {
                    String memberName = names[i].trim();
                    String memberEmail = (i < emails.length) ? emails[i].trim() : "";

                    if (memberName.isEmpty()) {
                        ra.addFlashAttribute("error", "Please enter name for Member " + (i + 1));
                        return "redirect:/register/" + reg.getEventId();
                    }
                    if (memberName.length() < 2) {
                        ra.addFlashAttribute("error", "Member " + (i + 1) + " name must be at least 2 characters");
                        return "redirect:/register/" + reg.getEventId();
                    }

                    if (memberEmail.isEmpty()) {
                        ra.addFlashAttribute("error", "Please enter email for Member " + (i + 1));
                        return "redirect:/register/" + reg.getEventId();
                    }
                    if (!EMAIL_PATTERN.matcher(memberEmail).matches()) {
                        ra.addFlashAttribute("error", "Enter a valid email for Member " + (i + 1));
                        return "redirect:/register/" + reg.getEventId();
                    }
                }

                // Store member data
                reg.setMemberNames(memberNames);
                reg.setMemberEmails(memberEmails);
            } else {
                // Solo event - clear team fields
                reg.setTeamName(null);
                reg.setMemberNames(null);
                reg.setMemberEmails(null);
            }

            // ==================== SAVE REGISTRATION ====================

            reg.setStatus("PENDING");
            reg.setEmailStatus("NOT_SENT");

            // Save registration
            Registration saved = regService.save(reg);

            // ==================== SAVE PAYMENT FILE ====================

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File destination = new File(uploadDir + fileName);
                file.transferTo(destination);

                Payment payment = new Payment();
                payment.setRegistrationId(saved.getId());
                payment.setFilePath(fileName);
                paymentrepo.save(payment);
            }

            // Success message based on team size
            int totalMembers = 1 + (memberNames != null && !memberNames.isEmpty() ? memberNames.split(",").length : 0);
            if (totalMembers == 1) {
                ra.addFlashAttribute("success", "Registration Successful! You have registered as a Solo participant.");
            } else {
                ra.addFlashAttribute("success", "Registration Successful! Your team of " + totalMembers + " members has been registered.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }

        return "redirect:/register/" + reg.getEventId();
    }
}