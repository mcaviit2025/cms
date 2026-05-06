package com.example.cms.service;

import com.example.cms.dto.ExportConfigDTO;
import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.entity.Score;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private EventService eventService;

    private static final String[] DEFAULT_FIELDS = {
            "SL No", "Participant Code", "Leader Name", "Leader Email", "Phone", "College",
            "Team Name", "Member Names", "Member Emails", "Participation Type", "Status",
            "UTR Number", "Amount Paid", "Payment Status", "Total Score", "Judge Name"
    };

    public byte[] exportParticipants(ExportConfigDTO config) throws IOException {
        Event event = eventService.getEventById(config.getEventId());
        List<Registration> registrations = registrationService.getRegistrationsByEventId(config.getEventId());

        // Filter by status
        if (!"ALL".equals(config.getParticipantStatus())) {
            registrations = registrations.stream()
                    .filter(r -> r.getStatus().equalsIgnoreCase(config.getParticipantStatus()))
                    .toList();
        }

        // Get scores
        List<Score> allScores = scoreService.getScoresByEventId(config.getEventId());
        Map<Integer, Score> scoreMap = allScores.stream()
                .collect(Collectors.toMap(Score::getRegistrationId, s -> s, (s1, s2) -> s1));

        // Define all possible fields with their headers and getters
        Map<String, FieldDefinition> fieldDefs = getFieldDefinitions(event, scoreMap);

        // Get selected fields or use all if none selected
        List<String> selectedFields = config.getSelectedFields();
        if (selectedFields == null || selectedFields.isEmpty()) {
            selectedFields = new ArrayList<>(fieldDefs.keySet());
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Participants");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create header row based on selected fields
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < selectedFields.size(); i++) {
                String field = selectedFields.get(i);
                FieldDefinition def = fieldDefs.get(field);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(def != null ? def.header : field);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            int rowNum = 1;
            int slNo = 1;

            for (Registration reg : registrations) {
                Row row = sheet.createRow(rowNum++);
                Score score = scoreMap.get(reg.getId());

                for (int i = 0; i < selectedFields.size(); i++) {
                    String field = selectedFields.get(i);
                    String value = getFieldValue(field, reg, event, score, slNo);
                    row.createCell(i).setCellValue(value);
                }
                slNo++;
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IOException("Failed to generate export: " + e.getMessage(), e);
        }
    }

    private Map<String, FieldDefinition> getFieldDefinitions(Event event, Map<Integer, Score> scoreMap) {
        Map<String, FieldDefinition> fields = new LinkedHashMap<>();
        fields.put("slNo", new FieldDefinition("SL No", (reg, score, slNo) -> String.valueOf(slNo)));
        fields.put("participantCode", new FieldDefinition("Participant Code", (reg, score, slNo) ->
                reg.getParticipantCode() != null ? reg.getParticipantCode() : "-"));
        fields.put("leaderName", new FieldDefinition("Leader Name", (reg, score, slNo) ->
                reg.getLeaderName() != null ? reg.getLeaderName() : ""));
        fields.put("leaderEmail", new FieldDefinition("Leader Email", (reg, score, slNo) ->
                reg.getLeaderEmail() != null ? reg.getLeaderEmail() : ""));
        fields.put("phone", new FieldDefinition("Phone", (reg, score, slNo) ->
                reg.getPhone() != null ? reg.getPhone() : ""));
        fields.put("college", new FieldDefinition("College", (reg, score, slNo) ->
                reg.getCollege() != null ? reg.getCollege() : ""));
        fields.put("teamName", new FieldDefinition("Team Name", (reg, score, slNo) ->
                reg.getTeamName() != null ? reg.getTeamName() : "Solo"));
        fields.put("memberNames", new FieldDefinition("Member Names", (reg, score, slNo) ->
                reg.getMemberNames() != null ? reg.getMemberNames() : ""));
        fields.put("memberEmails", new FieldDefinition("Member Emails", (reg, score, slNo) ->
                reg.getMemberEmails() != null ? reg.getMemberEmails() : ""));
        fields.put("participationType", new FieldDefinition("Participation Type", (reg, score, slNo) ->
                reg.getParticipationType() != null ? reg.getParticipationType() : "Solo"));
        fields.put("status", new FieldDefinition("Status", (reg, score, slNo) ->
                reg.getStatus() != null ? reg.getStatus() : "PENDING"));
        fields.put("utrNumber", new FieldDefinition("UTR Number", (reg, score, slNo) ->
                reg.getUtrNumber() != null ? reg.getUtrNumber() : ""));
        fields.put("amount", new FieldDefinition("Amount Paid (₹)", (reg, score, slNo) -> {
            if (!"APPROVED".equalsIgnoreCase(reg.getStatus())) return "0";
            if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                int memberCount = 1;
                if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                    memberCount += reg.getMemberNames().split(",").length;
                }
                return String.valueOf(event.getFee() * memberCount);
            }
            return String.valueOf(event.getFee());
        }));
        fields.put("paymentStatus", new FieldDefinition("Payment Status", (reg, score, slNo) -> {
            if ("APPROVED".equalsIgnoreCase(reg.getStatus())) return "Paid";
            if ("PENDING".equalsIgnoreCase(reg.getStatus())) return "Pending";
            return "Rejected";
        }));
        fields.put("totalScore", new FieldDefinition("Total Score", (reg, score, slNo) -> {
            if (score != null && score.getTotalScore() != null) return String.valueOf(score.getTotalScore());
            return "Not Evaluated";
        }));
        fields.put("judgeName", new FieldDefinition("Judge Name", (reg, score, slNo) -> {
            if (score != null && score.getJudgeId() != null) return "Judge ID: " + score.getJudgeId();
            return "Not Evaluated";
        }));

        return fields;
    }

    private String getFieldValue(String field, Registration reg, Event event, Score score, int slNo) {
        switch (field) {
            case "slNo": return String.valueOf(slNo);
            case "participantCode": return reg.getParticipantCode() != null ? reg.getParticipantCode() : "-";
            case "leaderName": return reg.getLeaderName() != null ? reg.getLeaderName() : "";
            case "leaderEmail": return reg.getLeaderEmail() != null ? reg.getLeaderEmail() : "";
            case "phone": return reg.getPhone() != null ? reg.getPhone() : "";
            case "college": return reg.getCollege() != null ? reg.getCollege() : "";
            case "teamName": return reg.getTeamName() != null ? reg.getTeamName() : "Solo";
            case "memberNames": return reg.getMemberNames() != null ? reg.getMemberNames() : "";
            case "memberEmails": return reg.getMemberEmails() != null ? reg.getMemberEmails() : "";
            case "participationType": return reg.getParticipationType() != null ? reg.getParticipationType() : "Solo";
            case "status": return reg.getStatus() != null ? reg.getStatus() : "PENDING";
            case "utrNumber": return reg.getUtrNumber() != null ? reg.getUtrNumber() : "";
            case "amount":
                if (!"APPROVED".equalsIgnoreCase(reg.getStatus())) return "0";
                if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
                    int memberCount = 1;
                    if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                        memberCount += reg.getMemberNames().split(",").length;
                    }
                    return String.valueOf(event.getFee() * memberCount);
                }
                return String.valueOf(event.getFee());
            case "paymentStatus":
                if ("APPROVED".equalsIgnoreCase(reg.getStatus())) return "Paid";
                if ("PENDING".equalsIgnoreCase(reg.getStatus())) return "Pending";
                return "Rejected";
            case "totalScore":
                if (score != null && score.getTotalScore() != null) return String.valueOf(score.getTotalScore());
                return "Not Evaluated";
            case "judgeName":
                if (score != null && score.getJudgeId() != null) return "Judge ID: " + score.getJudgeId();
                return "Not Evaluated";
            default: return "";
        }
    }

    // Inner class for field definition
    class FieldDefinition {
        String header;
        FieldValueExtractor extractor;

        FieldDefinition(String header, FieldValueExtractor extractor) {
            this.header = header;
            this.extractor = extractor;
        }
    }

    interface FieldValueExtractor {
        String extract(Registration reg, Score score, int slNo);
    }

    private double calculateAmountPaid(Registration reg, Event event) {
        if (!"APPROVED".equalsIgnoreCase(reg.getStatus())) {
            return 0;
        }
        if ("Group".equalsIgnoreCase(reg.getParticipationType())) {
            int memberCount = 1;
            if (reg.getMemberNames() != null && !reg.getMemberNames().isEmpty()) {
                memberCount += reg.getMemberNames().split(",").length;
            }
            return event.getFee() * memberCount;
        }
        return event.getFee();
    }

    private String getPaymentStatus(Registration reg) {
        if ("APPROVED".equalsIgnoreCase(reg.getStatus())) {
            return "Paid";
        } else if ("PENDING".equalsIgnoreCase(reg.getStatus())) {
            return "Pending";
        } else {
            return "Rejected";
        }
    }
}