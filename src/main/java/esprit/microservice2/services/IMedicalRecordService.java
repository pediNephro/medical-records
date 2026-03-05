package esprit.microservice2.services;

import esprit.microservice2.dto.MedicalRecordDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface IMedicalRecordService {
    MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO);

    MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO);

    void deleteMedicalRecord(Long id);

    Optional<MedicalRecordDTO> getMedicalRecordById(Long id);

    List<MedicalRecordDTO> getAllMedicalRecords();

    List<MedicalRecordDTO> getMedicalRecordsByPatientId(Long patientId);

    List<MedicalRecordDTO> getMedicalRecordsByDoctorId(Long doctorId);

    List<MedicalRecordDTO> getMedicalRecordsByArchiveStatus(Boolean isArchived);

    MedicalRecordDTO uploadImage(Long id, MultipartFile file);
}
