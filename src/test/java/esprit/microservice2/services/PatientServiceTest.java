package esprit.microservice2.services;

import esprit.microservice2.dto.PatientDTO;
import esprit.microservice2.entities.Patient;
import esprit.microservice2.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService - Tests unitaires")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Mohamed");
        patient.setLastName("Ben Ali");
        patient.setBirthDate(LocalDate.of(2010, 5, 15));
        patient.setGender("MALE");
        patient.setPhoneNumber("+21623456789");

        patientDTO = new PatientDTO(
                1L, "Mohamed", "Ben Ali",
                LocalDate.of(2010, 5, 15), "MALE", "+21623456789");
    }

    // ─── createPatient ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPatient - crée et retourne le DTO")
    void createPatient_success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientDTO result = patientService.createPatient(patientDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Mohamed");
        assertThat(result.getLastName()).isEqualTo("Ben Ali");
        assertThat(result.getGender()).isEqualTo("MALE");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("createPatient - mappe correctement tous les champs")
    void createPatient_mapsAllFields() {
        Patient saved = new Patient();
        saved.setId(2L);
        saved.setFirstName("Fatima");
        saved.setLastName("Zouari");
        saved.setBirthDate(LocalDate.of(2015, 3, 20));
        saved.setGender("FEMALE");
        saved.setPhoneNumber("+21698765432");

        PatientDTO dto = new PatientDTO(null, "Fatima", "Zouari",
                LocalDate.of(2015, 3, 20), "FEMALE", "+21698765432");

        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        PatientDTO result = patientService.createPatient(dto);

        assertThat(result.getFirstName()).isEqualTo("Fatima");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(2015, 3, 20));
        assertThat(result.getPhoneNumber()).isEqualTo("+21698765432");
    }

    // ─── updatePatient ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePatient - met à jour les champs si trouvé")
    void updatePatient_found_updatesAndReturns() {
        PatientDTO updateDTO = new PatientDTO(
                1L, "Mohammed", "Ben Ali Updated",
                LocalDate.of(2010, 5, 15), "MALE", "+21699999999");

        Patient updated = new Patient();
        updated.setId(1L);
        updated.setFirstName("Mohammed");
        updated.setLastName("Ben Ali Updated");
        updated.setBirthDate(LocalDate.of(2010, 5, 15));
        updated.setGender("MALE");
        updated.setPhoneNumber("+21699999999");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        PatientDTO result = patientService.updatePatient(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Mohammed");
        assertThat(result.getLastName()).isEqualTo("Ben Ali Updated");
        assertThat(result.getPhoneNumber()).isEqualTo("+21699999999");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("updatePatient - retourne null si patient introuvable")
    void updatePatient_notFound_returnsNull() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        PatientDTO result = patientService.updatePatient(99L, patientDTO);

        assertThat(result).isNull();
        verify(patientRepository, never()).save(any());
    }

    // ─── deletePatient ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletePatient - appelle deleteById avec le bon ID")
    void deletePatient_callsRepository() {
        doNothing().when(patientRepository).deleteById(1L);

        patientService.deletePatient(1L);

        verify(patientRepository).deleteById(1L);
    }

    // ─── getPatientById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPatientById - retourne un Optional avec le patient trouvé")
    void getPatientById_found_returnsOptional() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Optional<PatientDTO> result = patientService.getPatientById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getFirstName()).isEqualTo("Mohamed");
    }

    @Test
    @DisplayName("getPatientById - retourne Optional.empty() si introuvable")
    void getPatientById_notFound_returnsEmpty() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PatientDTO> result = patientService.getPatientById(99L);

        assertThat(result).isEmpty();
    }

    // ─── getAllPatients ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPatients - retourne la liste complète convertie en DTO")
    void getAllPatients_returnsMappedList() {
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setFirstName("Sara");
        patient2.setLastName("Mejri");
        patient2.setBirthDate(LocalDate.of(2008, 11, 3));
        patient2.setGender("FEMALE");
        patient2.setPhoneNumber("+21611112222");

        when(patientRepository.findAll()).thenReturn(List.of(patient, patient2));

        List<PatientDTO> result = patientService.getAllPatients();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PatientDTO::getFirstName)
                .containsExactlyInAnyOrder("Mohamed", "Sara");
    }

    @Test
    @DisplayName("getAllPatients - retourne liste vide si aucun patient")
    void getAllPatients_empty_returnsEmptyList() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<PatientDTO> result = patientService.getAllPatients();

        assertThat(result).isEmpty();
    }
}
