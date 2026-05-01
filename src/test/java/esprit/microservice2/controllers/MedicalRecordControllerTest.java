package esprit.microservice2.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import esprit.microservice2.dto.MedicalRecordDTO;
import esprit.microservice2.services.GeminiService;
import esprit.microservice2.services.IMedicalRecordService;
import esprit.microservice2.services.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = MedicalRecordController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@DisplayName("MedicalRecordController - Tests d'intégration (MockMvc)")
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMedicalRecordService medicalRecordService;

    @MockitoBean
    private GeminiService geminiService;

    @MockitoBean
    private PdfService pdfService;

    private ObjectMapper objectMapper;
    private MedicalRecordDTO recordDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        recordDTO = new MedicalRecordDTO(
                10L, 1L, 5L,
                "Insuffisance rénale chronique",
                "Dialyse 3x/semaine",
                "Contrôle mensuel requis",
                false, null,
                LocalDateTime.of(2026, 1, 10, 9, 0));
    }

    // ─── POST /api/medical-records ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/medical-records - 201 Created avec le dossier créé")
    void createMedicalRecord_success_returns201() throws Exception {
        when(medicalRecordService.createMedicalRecord(any(MedicalRecordDTO.class)))
                .thenReturn(recordDTO);

        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recordDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Insuffisance rénale chronique"))
                .andExpect(jsonPath("$.isArchived").value(false));
    }

    @Test
    @DisplayName("POST /api/medical-records - 400 Bad Request si patient introuvable")
    void createMedicalRecord_patientNotFound_returns400() throws Exception {
        when(medicalRecordService.createMedicalRecord(any(MedicalRecordDTO.class)))
                .thenThrow(new RuntimeException("Patient not found with id: 99"));

        MedicalRecordDTO bad = new MedicalRecordDTO(
                null, 99L, 5L, "Diagnostic", "Traitement", "Notes", false, null, null);

        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/medical-records ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/medical-records - 200 OK avec liste complète")
    void getAllMedicalRecords_returns200() throws Exception {
        MedicalRecordDTO record2 = new MedicalRecordDTO(
                11L, 1L, 6L, "Hypertension", "Antihypertenseurs",
                "Suivi hebdo", true, null, null);

        when(medicalRecordService.getAllMedicalRecords()).thenReturn(List.of(recordDTO, record2));

        mockMvc.perform(get("/api/medical-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    // ─── GET /api/medical-records/{id} ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/medical-records/{id} - 200 OK si dossier trouvé")
    void getMedicalRecordById_found_returns200() throws Exception {
        when(medicalRecordService.getMedicalRecordById(10L)).thenReturn(Optional.of(recordDTO));

        mockMvc.perform(get("/api/medical-records/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.treatment").value("Dialyse 3x/semaine"));
    }

    @Test
    @DisplayName("GET /api/medical-records/{id} - 404 Not Found si dossier inexistant")
    void getMedicalRecordById_notFound_returns404() throws Exception {
        when(medicalRecordService.getMedicalRecordById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medical-records/99"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/medical-records/{id} ───────────────────────────────────────

    @Test
    @DisplayName("PUT /api/medical-records/{id} - 200 OK avec dossier mis à jour")
    void updateMedicalRecord_found_returns200() throws Exception {
        MedicalRecordDTO updated = new MedicalRecordDTO(
                10L, 1L, 5L, "Nouveau diagnostic", "Nouveau traitement",
                "Nouvelles notes", true, null, null);

        when(medicalRecordService.updateMedicalRecord(eq(10L), any(MedicalRecordDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/medical-records/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Nouveau diagnostic"))
                .andExpect(jsonPath("$.isArchived").value(true));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id} - 404 si dossier introuvable")
    void updateMedicalRecord_notFound_returns404() throws Exception {
        when(medicalRecordService.updateMedicalRecord(eq(99L), any(MedicalRecordDTO.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/medical-records/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recordDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id} - 400 si patient introuvable lors update")
    void updateMedicalRecord_patientNotFound_returns400() throws Exception {
        when(medicalRecordService.updateMedicalRecord(eq(10L), any(MedicalRecordDTO.class)))
                .thenThrow(new RuntimeException("Patient not found with id: 99"));

        mockMvc.perform(put("/api/medical-records/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recordDTO)))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE /api/medical-records/{id} ────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/medical-records/{id} - 204 No Content après suppression")
    void deleteMedicalRecord_returns204() throws Exception {
        doNothing().when(medicalRecordService).deleteMedicalRecord(10L);

        mockMvc.perform(delete("/api/medical-records/10"))
                .andExpect(status().isNoContent());

        verify(medicalRecordService).deleteMedicalRecord(10L);
    }

    // ─── GET /api/medical-records/patient/{patientId} ────────────────────────

    @Test
    @DisplayName("GET /patient/{patientId} - 200 OK avec dossiers du patient")
    void getMedicalRecordsByPatientId_returns200() throws Exception {
        when(medicalRecordService.getMedicalRecordsByPatientId(1L))
                .thenReturn(List.of(recordDTO));

        mockMvc.perform(get("/api/medical-records/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    // ─── GET /api/medical-records/archived/{isArchived} ──────────────────────

    @Test
    @DisplayName("GET /archived/true - 200 OK avec dossiers archivés")
    void getMedicalRecordsByArchiveStatus_archived_returns200() throws Exception {
        MedicalRecordDTO archived = new MedicalRecordDTO(
                12L, 1L, 5L, "Ancien diagnostic", "Ancien traitement",
                "Notes archivées", true, null, null);

        when(medicalRecordService.getMedicalRecordsByArchiveStatus(true))
                .thenReturn(List.of(archived));

        mockMvc.perform(get("/api/medical-records/archived/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isArchived").value(true));
    }

    // ─── POST /api/medical-records/{id}/image ────────────────────────────────

    @Test
    @DisplayName("POST /{id}/image - 200 OK avec URL de l'image uploadée")
    void uploadImage_success_returns200() throws Exception {
        MedicalRecordDTO withImage = new MedicalRecordDTO(
                10L, 1L, 5L, "Insuffisance rénale chronique",
                "Dialyse 3x/semaine", "Contrôle mensuel requis",
                false, "https://cloudinary.com/scan.jpg", null);

        when(medicalRecordService.uploadImage(eq(10L), any())).thenReturn(withImage);

        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

        mockMvc.perform(multipart("/api/medical-records/10/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://cloudinary.com/scan.jpg"));
    }

    @Test
    @DisplayName("POST /{id}/image - 400 si dossier introuvable")
    void uploadImage_notFound_returns400() throws Exception {
        when(medicalRecordService.uploadImage(eq(99L), any()))
                .thenThrow(new RuntimeException("Medical record not found with id: 99"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

        mockMvc.perform(multipart("/api/medical-records/99/image").file(file))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/medical-records/{id}/analyze ───────────────────────────────

    @Test
    @DisplayName("POST /{id}/analyze - 200 OK avec analyse Gemini")
    void analyzeRecord_found_returns200() throws Exception {
        when(medicalRecordService.getMedicalRecordById(10L)).thenReturn(Optional.of(recordDTO));
        when(geminiService.analyzeMedicalRecord(recordDTO))
                .thenReturn("Analyse : risque modéré d'évolution vers insuffisance terminale.");

        mockMvc.perform(post("/api/medical-records/10/analyze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").value(
                        "Analyse : risque modéré d'évolution vers insuffisance terminale."));
    }

    @Test
    @DisplayName("POST /{id}/analyze - 404 si dossier introuvable")
    void analyzeRecord_notFound_returns404() throws Exception {
        when(medicalRecordService.getMedicalRecordById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/medical-records/99/analyze"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{id}/analyze - 500 si Gemini échoue")
    void analyzeRecord_geminiError_returns500() throws Exception {
        when(medicalRecordService.getMedicalRecordById(10L)).thenReturn(Optional.of(recordDTO));
        when(geminiService.analyzeMedicalRecord(any()))
                .thenThrow(new RuntimeException("Gemini API unavailable"));

        mockMvc.perform(post("/api/medical-records/10/analyze"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gemini API unavailable"));
    }

    // ─── GET /api/medical-records/{id}/analyze/pdf ───────────────────────────

    @Test
    @DisplayName("GET /{id}/analyze/pdf - 200 OK avec PDF en réponse")
    void analyzeAndDownloadPdf_found_returnsPdf() throws Exception {
        byte[] pdfBytes = "PDF-CONTENT".getBytes();

        when(medicalRecordService.getMedicalRecordById(10L)).thenReturn(Optional.of(recordDTO));
        when(geminiService.analyzeMedicalRecord(recordDTO)).thenReturn("Analyse complète");
        when(pdfService.generateAnalysisPdf(eq(recordDTO), eq("Analyse complète")))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/api/medical-records/10/analyze/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition",
                        "form-data; name=\"attachment\"; filename=\"analyse-dossier-10.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    @DisplayName("GET /{id}/analyze/pdf - 404 si dossier introuvable")
    void analyzeAndDownloadPdf_notFound_returns404() throws Exception {
        when(medicalRecordService.getMedicalRecordById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medical-records/99/analyze/pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{id}/analyze/pdf - 500 si génération PDF échoue")
    void analyzeAndDownloadPdf_pdfError_returns500() throws Exception {
        when(medicalRecordService.getMedicalRecordById(10L)).thenReturn(Optional.of(recordDTO));
        when(geminiService.analyzeMedicalRecord(any())).thenReturn("Analyse");
        when(pdfService.generateAnalysisPdf(any(), any()))
                .thenThrow(new RuntimeException("PDF generation failed"));

        mockMvc.perform(get("/api/medical-records/10/analyze/pdf"))
                .andExpect(status().isInternalServerError());
    }
}
