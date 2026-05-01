package esprit.microservice2.services;

import esprit.microservice2.dto.MedicalRecordDTO;
import esprit.microservice2.entities.MedicalRecord;
import esprit.microservice2.entities.Patient;
import esprit.microservice2.repositories.MedicalRecordRepository;
import esprit.microservice2.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService - Tests unitaires")
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Patient patient;
    private MedicalRecord record;
    private MedicalRecordDTO recordDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Mohamed");
        patient.setLastName("Ben Ali");
        patient.setBirthDate(LocalDate.of(2010, 5, 15));
        patient.setGender("MALE");

        record = new MedicalRecord();
        record.setId(10L);
        record.setPatient(patient);
        record.setDoctorId(5L);
        record.setDiagnosis("Insuffisance rénale chronique");
        record.setTreatment("Dialyse 3x/semaine");
        record.setNotes("Contrôle mensuel requis");
        record.setIsArchived(false);
        record.setCreatedAt(LocalDateTime.of(2026, 1, 10, 9, 0));

        recordDTO = new MedicalRecordDTO(
                10L, 1L, 5L,
                "Insuffisance rénale chronique",
                "Dialyse 3x/semaine",
                "Contrôle mensuel requis",
                false, null,
                LocalDateTime.of(2026, 1, 10, 9, 0));
    }

    // ─── createMedicalRecord ──────────────────────────────────────────────────

    @Test
    @DisplayName("createMedicalRecord - succès : patient trouvé")
    void createMedicalRecord_patientFound_success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(recordDTO);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getDiagnosis()).isEqualTo("Insuffisance rénale chronique");
        assertThat(result.getIsArchived()).isFalse();
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("createMedicalRecord - lève exception si patient introuvable")
    void createMedicalRecord_patientNotFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        MedicalRecordDTO dto = new MedicalRecordDTO(null, 99L, 5L,
                "Diagnostic", "Traitement", "Notes", false, null, null);

        assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found with id: 99");

        verify(medicalRecordRepository, never()).save(any());
    }

    // ─── updateMedicalRecord ──────────────────────────────────────────────────

    @Test
    @DisplayName("updateMedicalRecord - met à jour les champs si trouvé, même patient")
    void updateMedicalRecord_found_samePatient_updates() {
        MedicalRecordDTO updateDTO = new MedicalRecordDTO(
                10L, 1L, 5L,
                "Nouveau diagnostic", "Nouveau traitement",
                "Nouvelles notes", true, null, null);

        MedicalRecord updated = new MedicalRecord();
        updated.setId(10L);
        updated.setPatient(patient);
        updated.setDoctorId(5L);
        updated.setDiagnosis("Nouveau diagnostic");
        updated.setTreatment("Nouveau traitement");
        updated.setNotes("Nouvelles notes");
        updated.setIsArchived(true);

        when(medicalRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(updated);

        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(10L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getDiagnosis()).isEqualTo("Nouveau diagnostic");
        assertThat(result.getIsArchived()).isTrue();
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("updateMedicalRecord - change le patient si patientId différent")
    void updateMedicalRecord_differentPatient_swapsPatient() {
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setFirstName("Sara");
        patient2.setLastName("Mejri");
        patient2.setBirthDate(LocalDate.of(2008, 11, 3));
        patient2.setGender("FEMALE");

        MedicalRecordDTO updateDTO = new MedicalRecordDTO(
                10L, 2L, 5L, "Diagnostic", "Traitement", "Notes", false, null, null);

        MedicalRecord updated = new MedicalRecord();
        updated.setId(10L);
        updated.setPatient(patient2);
        updated.setDoctorId(5L);
        updated.setDiagnosis("Diagnostic");
        updated.setIsArchived(false);

        when(medicalRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient2));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(updated);

        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(10L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPatientId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateMedicalRecord - retourne null si dossier introuvable")
    void updateMedicalRecord_notFound_returnsNull() {
        when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(99L, recordDTO);

        assertThat(result).isNull();
        verify(medicalRecordRepository, never()).save(any());
    }

    // ─── deleteMedicalRecord ──────────────────────────────────────────────────

    @Test
    @DisplayName("deleteMedicalRecord - appelle deleteById avec le bon ID")
    void deleteMedicalRecord_callsRepository() {
        doNothing().when(medicalRecordRepository).deleteById(10L);

        medicalRecordService.deleteMedicalRecord(10L);

        verify(medicalRecordRepository).deleteById(10L);
    }

    // ─── getMedicalRecordById ─────────────────────────────────────────────────

    @Test
    @DisplayName("getMedicalRecordById - retourne Optional avec le dossier trouvé")
    void getMedicalRecordById_found_returnsOptional() {
        when(medicalRecordRepository.findById(10L)).thenReturn(Optional.of(record));

        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getDiagnosis()).isEqualTo("Insuffisance rénale chronique");
    }

    @Test
    @DisplayName("getMedicalRecordById - retourne Optional.empty() si introuvable")
    void getMedicalRecordById_notFound_returnsEmpty() {
        when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordById(99L);

        assertThat(result).isEmpty();
    }

    // ─── getAllMedicalRecords ─────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMedicalRecords - retourne tous les dossiers convertis")
    void getAllMedicalRecords_returnsList() {
        MedicalRecord record2 = new MedicalRecord();
        record2.setId(11L);
        record2.setPatient(patient);
        record2.setDoctorId(6L);
        record2.setDiagnosis("Hypertension");
        record2.setIsArchived(true);

        when(medicalRecordRepository.findAll()).thenReturn(List.of(record, record2));

        List<MedicalRecordDTO> result = medicalRecordService.getAllMedicalRecords();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MedicalRecordDTO::getId)
                .containsExactlyInAnyOrder(10L, 11L);
    }

    // ─── getMedicalRecordsByPatientId ─────────────────────────────────────────

    @Test
    @DisplayName("getMedicalRecordsByPatientId - retourne les dossiers du patient")
    void getMedicalRecordsByPatientId_returnsList() {
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(List.of(record));

        List<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsByPatientId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMedicalRecordsByPatientId - retourne liste vide si aucun dossier")
    void getMedicalRecordsByPatientId_empty_returnsEmptyList() {
        when(medicalRecordRepository.findByPatientId(99L)).thenReturn(List.of());

        List<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsByPatientId(99L);

        assertThat(result).isEmpty();
    }

    // ─── getMedicalRecordsByArchiveStatus ─────────────────────────────────────

    @Test
    @DisplayName("getMedicalRecordsByArchiveStatus - retourne les dossiers archivés")
    void getMedicalRecordsByArchiveStatus_archived_returnsList() {
        MedicalRecord archived = new MedicalRecord();
        archived.setId(12L);
        archived.setPatient(patient);
        archived.setDoctorId(5L);
        archived.setDiagnosis("Ancien diagnostic");
        archived.setIsArchived(true);

        when(medicalRecordRepository.findByIsArchived(true)).thenReturn(List.of(archived));

        List<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsByArchiveStatus(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsArchived()).isTrue();
    }

    // ─── uploadImage ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("uploadImage - met à jour l'URL de l'image dans le dossier")
    void uploadImage_success_updatesImageUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

        when(medicalRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(cloudinaryService.uploadImage(file, 10L))
                .thenReturn("https://cloudinary.com/scan.jpg");

        MedicalRecord saved = new MedicalRecord();
        saved.setId(10L);
        saved.setPatient(patient);
        saved.setDoctorId(5L);
        saved.setDiagnosis("Insuffisance rénale chronique");
        saved.setIsArchived(false);
        saved.setImageUrl("https://cloudinary.com/scan.jpg");

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(saved);

        MedicalRecordDTO result = medicalRecordService.uploadImage(10L, file);

        assertThat(result.getImageUrl()).isEqualTo("https://cloudinary.com/scan.jpg");
        verify(cloudinaryService).uploadImage(file, 10L);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("uploadImage - lève exception si dossier introuvable")
    void uploadImage_recordNotFound_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

        when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.uploadImage(99L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medical record not found with id: 99");

        verify(cloudinaryService, never()).uploadImage(any(), any());
    }
}
