package com.example.cms.controller;

import com.example.cms.entity.Event;
import com.example.cms.entity.EvaluationCriteria;
import com.example.cms.entity.Judge;
import com.example.cms.entity.Registration;
import com.example.cms.entity.Score;
import com.example.cms.repository.EventJudgeRepository;
import com.example.cms.repository.EventRepository;
import com.example.cms.repository.EvaluationCriteriaRepository;
import com.example.cms.repository.JudgeRepository;
import com.example.cms.repository.RegistrationRepository;
import com.example.cms.repository.ScoreRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class JudgeController {

    @Autowired
    private JudgeRepository judgeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private EvaluationCriteriaRepository criteriaRepository;

    @Autowired
    private EventJudgeRepository eventJudgeRepository;

    @GetMapping("/judge/login")
    public String judgeLogin() {
        return "judge-login";
    }

    @PostMapping("/judge/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Judge> judgeOpt = judgeRepository.findByUsername(username);

            if (judgeOpt.isPresent()) {
                Judge judge = judgeOpt.get();
                if (judge.getPassword().equals(password) && judge.getIsActive()) {
                    session.setAttribute("judgeLoggedIn", true);
                    session.setAttribute("judgeId", judge.getId());
                    session.setAttribute("judgeName", judge.getFullName());
                    System.out.println("Judge logged in: " + judge.getFullName() + " (ID: " + judge.getId() + ")");
                    return "redirect:/judge/dashboard";
                }
            }
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/judge/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/judge/login";
        }
    }

    @GetMapping("/judge/dashboard")
    public String judgeDashboard(Model model, HttpSession session) {
        if (session.getAttribute("judgeLoggedIn") == null) {
            return "redirect:/judge/login";
        }

        try {
            Integer judgeId = (Integer) session.getAttribute("judgeId");
            String judgeName = (String) session.getAttribute("judgeName");

            System.out.println("=== Judge Dashboard ===");
            System.out.println("Judge ID: " + judgeId);

            // Get events assigned to this judge
            List<Integer> assignedEventIds = eventJudgeRepository.findEventIdsByJudgeId(judgeId);
            System.out.println("Assigned Event IDs: " + assignedEventIds);

            List<Event> assignedEvents = new ArrayList<>();
            if (assignedEventIds != null && !assignedEventIds.isEmpty()) {
                assignedEvents = eventRepository.findAllById(assignedEventIds);
            }

            // Calculate statistics for each assigned event
            for (Event event : assignedEvents) {
                System.out.println("Processing Event: " + event.getEventName());

                // Get all registrations for this event
                List<Registration> allRegistrations = registrationRepository.findByEventId(event.getEventId());

                // Count only APPROVED registrations
                List<Registration> approvedRegistrations = allRegistrations.stream()
                        .filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                        .collect(Collectors.toList());

                int totalParticipants = approvedRegistrations.size();
                System.out.println("  Total Participants: " + totalParticipants);

                // Get scores by this judge
                List<Score> myScores = scoreRepository.findByEventIdAndJudgeId(event.getEventId(), judgeId);
                int evaluatedByMe = myScores.size();
                System.out.println("  Evaluated By Me: " + evaluatedByMe);

                // Get all scores for this event
                List<Score> allScores = scoreRepository.findByEventId(event.getEventId());
                int evaluatedByOthers = allScores.size() - evaluatedByMe;
                System.out.println("  Evaluated By Others: " + evaluatedByOthers);

                // Calculate pending (approved registrations not evaluated by anyone)
                Set<Integer> evaluatedRegistrationIds = allScores.stream()
                        .map(Score::getRegistrationId)
                        .collect(Collectors.toSet());

                int pendingParticipants = (int) approvedRegistrations.stream()
                        .filter(r -> !evaluatedRegistrationIds.contains(r.getId()))
                        .count();
                System.out.println("  Pending: " + pendingParticipants);

                // Set stats on event (using transient fields)
                event.setTotalParticipants(totalParticipants);
                event.setEvaluatedByMe(evaluatedByMe);
                event.setEvaluatedByOthers(evaluatedByOthers);
                event.setPendingParticipants(pendingParticipants);
            }

            model.addAttribute("events", assignedEvents);
            model.addAttribute("judgeName", judgeName);

            if (assignedEvents.isEmpty()) {
                model.addAttribute("error", "No events assigned to you yet. Please contact administrator.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }

        return "judge-dashboard";
    }

    @GetMapping("/judge/event/{eventId}/participants")
    public String eventParticipants(@PathVariable Integer eventId, Model model, HttpSession session) {
        if (session.getAttribute("judgeLoggedIn") == null) {
            return "redirect:/judge/login";
        }

        try {
            Integer judgeId = (Integer) session.getAttribute("judgeId");
            String judgeName = (String) session.getAttribute("judgeName");

            System.out.println("=== Event Participants ===");
            System.out.println("Event ID: " + eventId);
            System.out.println("Judge ID: " + judgeId);

            // Check if judge is assigned to this event
            boolean isAssigned = eventJudgeRepository.existsByEventIdAndJudgeId(eventId, judgeId);
            if (!isAssigned) {
                model.addAttribute("error", "You are not assigned to this event!");
                return "redirect:/judge/dashboard";
            }

            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                return "redirect:/judge/dashboard";
            }

            // Get evaluation criteria for this event
            List<EvaluationCriteria> criteria = criteriaRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
            int totalMaxMarks = 0;
            for (EvaluationCriteria c : criteria) {
                totalMaxMarks += c.getMaxMarks();
            }
            System.out.println("Criteria found: " + criteria.size());

            // Get only APPROVED registrations for this event
            List<Registration> allRegistrations = registrationRepository.findByEventId(eventId);
            List<Registration> approvedRegistrations = new ArrayList<>();
            for (Registration reg : allRegistrations) {
                if ("APPROVED".equalsIgnoreCase(reg.getStatus())) {
                    approvedRegistrations.add(reg);
                }
            }
            System.out.println("Approved registrations: " + approvedRegistrations.size());

            // Get scores by this judge
            List<Score> myScores = scoreRepository.findByEventIdAndJudgeId(eventId, judgeId);
            Set<Integer> myScoredIds = new HashSet<>();
            for (Score s : myScores) {
                myScoredIds.add(s.getRegistrationId());
            }
            System.out.println("My scored registrations: " + myScoredIds.size());

            // Get all scores for this event
            List<Score> allScores = scoreRepository.findByEventId(eventId);
            Map<Integer, Score> scoreMap = new HashMap<>();
            for (Score s : allScores) {
                scoreMap.put(s.getRegistrationId(), s);
            }

            // Get judge names for "Evaluated By Others" section
            Map<Integer, String> judgeNameMap = new HashMap<>();
            for (Score s : allScores) {
                if (!s.getJudgeId().equals(judgeId)) {
                    Optional<Judge> judgeOpt = judgeRepository.findById(s.getJudgeId());
                    if (judgeOpt.isPresent()) {
                        judgeNameMap.put(s.getRegistrationId(), judgeOpt.get().getFullName());
                    }
                }
            }

            // Separate participants into 3 categories
            List<Registration> notEvaluated = new ArrayList<>();
            List<Registration> evaluatedByMe = new ArrayList<>();
            List<Registration> evaluatedByOthers = new ArrayList<>();

            for (Registration reg : approvedRegistrations) {
                if (myScoredIds.contains(reg.getId())) {
                    evaluatedByMe.add(reg);
                } else if (scoreMap.containsKey(reg.getId())) {
                    evaluatedByOthers.add(reg);
                } else {
                    notEvaluated.add(reg);
                }
            }

            System.out.println("Not Evaluated: " + notEvaluated.size());
            System.out.println("Evaluated By Me: " + evaluatedByMe.size());
            System.out.println("Evaluated By Others: " + evaluatedByOthers.size());

            // Create map of my scores for easy access
            Map<Integer, Score> myScoresMap = new HashMap<>();
            for (Score s : myScores) {
                myScoresMap.put(s.getRegistrationId(), s);
            }

            model.addAttribute("event", event);
            model.addAttribute("judgeName", judgeName);
            model.addAttribute("criteria", criteria);
            model.addAttribute("totalMaxMarks", totalMaxMarks);
            model.addAttribute("notEvaluated", notEvaluated);
            model.addAttribute("evaluatedByMe", evaluatedByMe);
            model.addAttribute("evaluatedByOthers", evaluatedByOthers);
            model.addAttribute("myScoresMap", myScoresMap);
            model.addAttribute("scoreMap", scoreMap);
            model.addAttribute("judgeNameMap", judgeNameMap);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading page: " + e.getMessage());
        }

        return "judge/event-participants";
    }

    @PostMapping("/judge/save-score")
    public String saveScore(@RequestParam Map<String, String> allParams,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            Integer judgeId = (Integer) session.getAttribute("judgeId");

            if (judgeId == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
                return "redirect:/judge/login";
            }

            Integer registrationId = null;
            Integer eventId = null;
            String comments = null;
            Map<String, Integer> scoresMap = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("registrationId")) {
                    registrationId = Integer.parseInt(value);
                } else if (key.equals("eventId")) {
                    eventId = Integer.parseInt(value);
                } else if (key.equals("comments")) {
                    comments = value;
                } else if (key.startsWith("criteria_")) {
                    String criteriaId = key.substring(9);
                    try {
                        int score = Integer.parseInt(value);
                        scoresMap.put(criteriaId, score);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }

            if (registrationId == null || eventId == null) {
                redirectAttributes.addFlashAttribute("error", "Missing registration or event information");
                return "redirect:/judge/dashboard";
            }

            // Calculate total score
            int totalScore = 0;
            for (Map.Entry<String, Integer> entry : scoresMap.entrySet()) {
                totalScore += entry.getValue();
            }

            // Build JSON string
            StringBuilder scoresJson = new StringBuilder();
            scoresJson.append("{");
            int count = 0;
            for (Map.Entry<String, Integer> entry : scoresMap.entrySet()) {
                if (count > 0) scoresJson.append(",");
                scoresJson.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
                count++;
            }
            scoresJson.append("}");
            String scoresData = scoresJson.toString();

            // Check if score already exists
            Optional<Score> existingScore = scoreRepository.findByRegistrationIdAndJudgeId(registrationId, judgeId);

            Score score;
            if (existingScore.isPresent()) {
                score = existingScore.get();
                score.setTotalScore(totalScore);
                score.setScoresData(scoresData);
                score.setComments(comments);
                System.out.println("Updated score for registration: " + registrationId);
            } else {
                score = new Score();
                score.setRegistrationId(registrationId);
                score.setEventId(eventId);
                score.setJudgeId(judgeId);
                score.setTotalScore(totalScore);
                score.setScoresData(scoresData);
                score.setComments(comments);
                score.setIsFinalized(false);
                System.out.println("Created new score for registration: " + registrationId);
            }

            scoreRepository.save(score);
            System.out.println("Score saved successfully!");

            redirectAttributes.addFlashAttribute("success", "Evaluation saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save: " + e.getMessage());
        }

        String eventId = allParams.get("eventId");
        return "redirect:/judge/event/" + (eventId != null ? eventId : "0") + "/participants";
    }

    @GetMapping("/judge/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/judge/login";
    }
}