package com.example.cms.serviceImp;

import com.example.cms.entity.Event;
import com.example.cms.entity.Registration;
import com.example.cms.service.CertificateService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Override
    public ByteArrayInputStream generateCertificate(Registration registration, Event event, String memberName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc);
            document.setMargins(50, 50, 50, 50);

            // Add decorative border
            Table borderTable = new Table(UnitValue.createPercentArray(1));
            borderTable.setWidth(UnitValue.createPercentValue(100));
            borderTable.setBorder(new SolidBorder(ColorConstants.BLACK, 2));
            document.add(borderTable);

            document.add(new Paragraph("\n\n"));

            // Title
            Paragraph title = new Paragraph("TECHFEST 2025")
                    .setFontSize(32)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Certificate of Participation
            Paragraph certTitle = new Paragraph("CERTIFICATE OF PARTICIPATION")
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(certTitle);

            document.add(new Paragraph("\n\n"));

            // This is to certify that
            Paragraph certifyText = new Paragraph("This is to certify that")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(certifyText);

            document.add(new Paragraph("\n"));

            // Student Name
            Paragraph studentName = new Paragraph(memberName)
                    .setFontSize(26)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE);
            document.add(studentName);

            document.add(new Paragraph("\n"));

            // has successfully participated in
            Paragraph participatedText = new Paragraph("has successfully participated in")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(participatedText);

            document.add(new Paragraph("\n"));

            // Event Name
            Paragraph eventName = new Paragraph(event.getEventName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(eventName);

            document.add(new Paragraph("\n"));

            // Team Name (for group events)
            if (registration.getTeamName() != null && !registration.getTeamName().isEmpty()) {
                Paragraph teamInfo = new Paragraph()
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER);
                teamInfo.add(new Text("Team: ").setBold());
                teamInfo.add(registration.getTeamName());
                document.add(teamInfo);
                document.add(new Paragraph("\n"));
            }

            // Event details
            String formattedDate = "TBD";
            if (event.getEventDate() != null && !event.getEventDate().isEmpty()) {
                formattedDate = event.getEventDate();
            }

            Paragraph details = new Paragraph()
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            details.add("Held on " + formattedDate);
            if (event.getVenue() != null && !event.getVenue().isEmpty()) {
                details.add(" at " + event.getVenue());
            }
            document.add(details);

            document.add(new Paragraph("\n\n\n"));

            // Signature and Stamp Section
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            footerTable.setWidth(UnitValue.createPercentValue(80));
            footerTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

            // Signature
            Cell signatureCell = new Cell();
            signatureCell.setBorder(null);
            Paragraph signatureTitle = new Paragraph("_________________________")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            Paragraph signatureName = new Paragraph("Event Coordinator")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            signatureCell.add(signatureTitle);
            signatureCell.add(signatureName);

            // Stamp
            Cell stampCell = new Cell();
            stampCell.setBorder(null);
            Paragraph stampTitle = new Paragraph("_________________________")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            Paragraph stampName = new Paragraph("TechFest 2025")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            stampCell.add(stampTitle);
            stampCell.add(stampName);

            footerTable.addCell(signatureCell);
            footerTable.addCell(stampCell);
            document.add(footerTable);

            document.add(new Paragraph("\n\n"));

            // Current Date
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy");
            String currentDate = LocalDate.now().format(dateFormatter);
            Paragraph datePara = new Paragraph("Date: " + currentDate)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(datePara);

            document.add(new Paragraph("\n"));

            // Certificate ID
            String certId = "TF-" + event.getEventId() + "-" + registration.getId() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            Paragraph certIdPara = new Paragraph("Certificate ID: " + certId)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(certIdPara);

            document.close();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate: " + e.getMessage());
        }
    }
}