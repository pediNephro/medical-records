package esprit.microservice2.services;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import esprit.microservice2.dto.MedicalRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Slf4j
public class PdfService {

    public byte[] generateAnalysisPdf(MedicalRecordDTO record, String analysis) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("ANALYSE DOSSIER MÉDICAL")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Date
            Paragraph dateP = new Paragraph("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(dateP);

            // Separator
            document.add(new Paragraph("\n"));

            // Patient & Doctor Info
            Table infoTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            infoTable.addCell(new Cell().add(new Paragraph("Numéro Dossier: " + record.getId()).setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("Patient ID: " + record.getPatientId())));
            infoTable.addCell(new Cell().add(new Paragraph("Médecin ID: " + record.getDoctorId()).setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("Date création: " + formatDate(record.getCreatedAt()))));
            document.add(infoTable);

            document.add(new Paragraph("\n"));

            // Medical Information
            Paragraph medicalHeader = new Paragraph("INFORMATIONS MÉDICALES")
                    .setFontSize(14)
                    .setBold();
            document.add(medicalHeader);

            Paragraph diagnosisLabel = new Paragraph("Diagnostic: ").setBold();
            Paragraph diagnosis = new Paragraph(record.getDiagnosis());
            document.add(diagnosisLabel);
            document.add(diagnosis);

            if (record.getTreatment() != null && !record.getTreatment().isEmpty()) {
                Paragraph treatmentLabel = new Paragraph("Traitement: ").setBold();
                Paragraph treatment = new Paragraph(record.getTreatment());
                document.add(treatmentLabel);
                document.add(treatment);
            }

            if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                Paragraph notesLabel = new Paragraph("Notes: ").setBold();
                Paragraph notes = new Paragraph(record.getNotes());
                document.add(notesLabel);
                document.add(notes);
            }

            document.add(new Paragraph("\n"));

            // AI Analysis
            Paragraph analysisHeader = new Paragraph("ANALYSE IA (Gemini Flash 2.5)")
                    .setFontSize(14)
                    .setBold();
            document.add(analysisHeader);

            Paragraph analysisContent = new Paragraph(analysis);
            document.add(analysisContent);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
