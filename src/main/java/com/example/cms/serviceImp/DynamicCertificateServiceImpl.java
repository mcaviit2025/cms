package com.example.cms.serviceImp;

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
            byte[] pdf = convertHtmlToPdf(html);

            if (pdf == null || pdf.length == 0) {
                throw new RuntimeException("PDF content is empty");
            }
            return pdf;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    @Override
    public String generateCertificateHtml(Registration registration, Event event, String memberName) {
        String template = getTemplate();
        String certificateId = "TF-" + event.getEventId() + "-" + registration.getId();

        return template
                .replace("{{studentName}}", memberName)
                .replace("{{eventName}}", event.getEventName())
                .replace("{{eventDate}}", event.getEventDate() != null ? event.getEventDate() : "")
                .replace("{{certificateId}}", certificateId);
    }

    private String getTemplate() {
        if (templateCache == null) {
            try {
                ClassPathResource resource = new ClassPathResource("templates/certificates/certificate-template.html");
                templateCache = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Could not load HTML template", e);
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
            return outputStream.toByteArray();
        }
    }
}