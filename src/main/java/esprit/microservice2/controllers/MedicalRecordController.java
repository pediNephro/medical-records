package esprit.microservice2.controllers;

import esprit.microservice2.dto.MedicalRecordDTO;
import esprit.microservice2.services.GeminiService;
import esprit.microservice2.services.IMedicalRecordService;
import esprit.microservice2.services.PdfService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final IMedicalRecordService medicalRecordService;
    private final GeminiService geminiService;
    private final PdfService pdfService;

    // ✅ CONSTRUCTEUR (SOLUTION AU PROBLÈME)
    public MedicalRecordController(IMedicalRecordService medicalRecordService,
                                   GeminiService geminiService,
                                   PdfService pdfService) {
        this.medicalRecordService = medicalRecordService;
        this.geminiService = geminiService;
        this.pdfService = pdfService;
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(@RequestBody MedicalRecordDTO medicalRecordDTO) {
        try {
            MedicalRecordDTO createdRecord = medicalRecordService.createMedicalRecord(medicalRecordDTO);
            return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<List<MedicalRecordDTO>> getAllMedicalRecords() {
        List<MedicalRecordDTO> records = medicalRecordService.getAllMedicalRecords();
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordById(@PathVariable Long id) {
        Optional<MedicalRecordDTO> record = medicalRecordService.getMedicalRecordById(id);
        return record.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecordDTO medicalRecordDTO) {

        try {
            MedicalRecordDTO updatedRecord = medicalRecordService.updateMedicalRecord(id, medicalRecordDTO);
            if (updatedRecord != null) {
                return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ================= BY PATIENT =================
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDTO>> getMedicalRecordsByPatientId(@PathVariable Long patientId) {
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByPatientId(patientId);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // ================= BY DOCTOR =================
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecordDTO>> getMedicalRecordsByDoctorId(@PathVariable Long doctorId) {
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByDoctorId(doctorId);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // ================= ARCHIVE =================
    @GetMapping("/archived/{isArchived}")
    public ResponseEntity<List<MedicalRecordDTO>> getMedicalRecordsByArchiveStatus(@PathVariable Boolean isArchived) {
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByArchiveStatus(isArchived);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // ================= UPLOAD IMAGE =================
    @PostMapping("/{id}/image")
    public ResponseEntity<MedicalRecordDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            MedicalRecordDTO updated = medicalRecordService.uploadImage(id, file);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================= IA ANALYSE =================
    @PostMapping("/{id}/analyze")
    public ResponseEntity<Map<String, String>> analyzeRecord(@PathVariable Long id) {

        try {
            Optional<MedicalRecordDTO> record = medicalRecordService.getMedicalRecordById(id);

            if (record.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String analysis = geminiService.analyzeMedicalRecord(record.get());

            return ResponseEntity.ok(Map.of("analysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= PDF =================
    @GetMapping("/{id}/analyze/pdf")
    public ResponseEntity<byte[]> analyzeAndDownloadPdf(@PathVariable Long id) {

        try {
            Optional<MedicalRecordDTO> record = medicalRecordService.getMedicalRecordById(id);

            if (record.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String analysis = geminiService.analyzeMedicalRecord(record.get());
            byte[] pdfBytes = pdfService.generateAnalysisPdf(record.get(), analysis);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "analyse-dossier-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}