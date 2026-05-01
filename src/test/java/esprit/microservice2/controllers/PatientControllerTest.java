package esprit.microservice2.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import esprit.microservice2.dto.PatientDTO;
import esprit.microservice2.services.IPatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = PatientController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@DisplayName("PatientController - Tests d'intégration (MockMvc)")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPatientService patientService;

    private ObjectMapper objectMapper;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        patientDTO = new PatientDTO(
                1L, "Mohamed", "Ben Ali",
                LocalDate.of(2010, 5, 15), "MALE", "+21623456789");
    }

    // ─── POST /api/patients ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/patients - 201 Created avec le patient créé")
    void createPatient_returns201() throws Exception {
        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(patientDTO);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Mohamed"))
                .andExpect(jsonPath("$.lastName").value("Ben Ali"))
                .andExpect(jsonPath("$.gender").value("MALE"));

        verify(patientService).createPatient(any(PatientDTO.class));
    }

    // ─── GET /api/patients ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/patients - 200 OK avec liste de patients")
    void getAllPatients_returns200WithList() throws Exception {
        PatientDTO patient2 = new PatientDTO(
                2L, "Sara", "Mejri",
                LocalDate.of(2008, 11, 3), "FEMALE", "+21611112222");

        when(patientService.getAllPatients()).thenReturn(List.of(patientDTO, patient2));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Mohamed"))
                .andExpect(jsonPath("$[1].firstName").value("Sara"));
    }

    @Test
    @DisplayName("GET /api/patients - 200 OK avec liste vide")
    void getAllPatients_empty_returns200() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/patients/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/patients/{id} - 200 OK si patient trouvé")
    void getPatientById_found_returns200() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(patientDTO));

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Mohamed"))
                .andExpect(jsonPath("$.phoneNumber").value("+21623456789"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - 404 Not Found si patient inexistant")
    void getPatientById_notFound_returns404() throws Exception {
        when(patientService.getPatientById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/99"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/patients/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/patients/{id} - 200 OK si patient mis à jour")
    void updatePatient_found_returns200() throws Exception {
        PatientDTO updated = new PatientDTO(
                1L, "Mohammed", "Ben Ali Updated",
                LocalDate.of(2010, 5, 15), "MALE", "+21699999999");

        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mohammed"))
                .andExpect(jsonPath("$.phoneNumber").value("+21699999999"));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} - 404 Not Found si patient inexistant")
    void updatePatient_notFound_returns404() throws Exception {
        when(patientService.updatePatient(eq(99L), any(PatientDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/patients/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/patients/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/patients/{id} - 204 No Content après suppression")
    void deletePatient_returns204() throws Exception {
        doNothing().when(patientService).deletePatient(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatient(1L);
    }
}
