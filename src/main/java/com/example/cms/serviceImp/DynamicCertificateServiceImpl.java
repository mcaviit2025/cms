package com.example.cms.serviceimpl;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.DynamicCertificateService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class DynamicCertificateServiceImpl implements DynamicCertificateService {

    private String templateCache;

    @Override
    public byte[] generateCertificatePdf(Registration registration, Event event, String memberName) {
        try {
            String html = generateCertificateHtml(registration, event, memberName);
            return convertHtmlToPdf(html);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate certificate PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateCertificateHtml(Registration registration, Event event, String memberName) {
        String template = getTemplate();

        // Format date
        String formattedDate = "TBD";
        if (event.getEventDate() != null && !event.getEventDate().isEmpty()) {
            formattedDate = event.getEventDate();
        }

        // Generate unique certificate ID
        String certificateId = "TF-" + event.getEventId() + "-" + registration.getId() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Handle team name
        String teamName = "";
        if (registration.getTeamName() != null && !registration.getTeamName().isEmpty()) {
            teamName = registration.getTeamName();
        }

        // Replace all placeholders
        String html = template
                .replace("{{studentName}}", memberName)
                .replace("{{eventName}}", event.getEventName())
                .replace("{{eventDate}}", formattedDate)
                .replace("{{certificateId}}", certificateId)
                .replace("{{teamName}}", teamName);

        return html;
    }

    private String getTemplate() {
        if (templateCache == null) {
            try {
                ClassPathResource resource = new ClassPathResource("templates/certificates/certificate-template.html");
                templateCache = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("✅ Certificate template loaded successfully");
            } catch (Exception e) {
                System.err.println("❌ Failed to load certificate template: " + e.getMessage());
                throw new RuntimeException("Failed to load certificate template", e);
            }
        }
        return templateCache;
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            System.out.println("✅ PDF generated successfully, size: " + outputStream.size() + " bytes");
            return outputStream.toByteArray();
        }
    }
}