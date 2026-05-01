package esprit.microservice2.services;

import esprit.microservice2.dto.MedicalRecordDTO;
import esprit.microservice2.entities.MedicalRecord;
import esprit.microservice2.entities.Patient;
import esprit.microservice2.repositories.MedicalRecordRepository;
import esprit.microservice2.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService implements IMedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO) {
        Optional<Patient> patientOptional = patientRepository.findById(medicalRecordDTO.getPatientId());
        if (!patientOptional.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + medicalRecordDTO.getPatientId());
        }

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patientOptional.get());
        medicalRecord.setDoctorId(medicalRecordDTO.getDoctorId());
        medicalRecord.setDiagnosis(medicalRecordDTO.getDiagnosis());
        medicalRecord.setTreatment(medicalRecordDTO.getTreatment());
        medicalRecord.setNotes(medicalRecordDTO.getNotes());
        medicalRecord.setIsArchived(false);

        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);
        return convertToDTO(savedRecord);
    }

    @Override
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO) {
        Optional<MedicalRecord> recordOptional = medicalRecordRepository.findById(id);
        if (recordOptional.isPresent()) {
            MedicalRecord medicalRecord = recordOptional.get();

            if (!medicalRecord.getPatient().getId().equals(medicalRecordDTO.getPatientId())) {
                Optional<Patient> patientOptional = patientRepository.findById(medicalRecordDTO.getPatientId());
                if (!patientOptional.isPresent()) {
                    throw new RuntimeException("Patient not found with id: " + medicalRecordDTO.getPatientId());
                }
                medicalRecord.setPatient(patientOptional.get());
            }

            medicalRecord.setDoctorId(medicalRecordDTO.getDoctorId());
            medicalRecord.setDiagnosis(medicalRecordDTO.getDiagnosis());
            medicalRecord.setTreatment(medicalRecordDTO.getTreatment());
            medicalRecord.setNotes(medicalRecordDTO.getNotes());
            medicalRecord.setIsArchived(medicalRecordDTO.getIsArchived());

            MedicalRecord updatedRecord = medicalRecordRepository.save(medicalRecord);
            return convertToDTO(updatedRecord);
        }
        return null;
    }

    @Override
    public void deleteMedicalRecord(Long id) {
        medicalRecordRepository.deleteById(id);
    }

    @Override
    public Optional<MedicalRecordDTO> getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<MedicalRecordDTO> getAllMedicalRecords() {
        return medicalRecordRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByDoctorId(Long doctorId) {
        return medicalRecordRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordDTO> getMedicalRecordsByArchiveStatus(Boolean isArchived) {
        return medicalRecordRepository.findByIsArchived(isArchived)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MedicalRecordDTO uploadImage(Long id, MultipartFile file) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id));
        String imageUrl = cloudinaryService.uploadImage(file, id);
        record.setImageUrl(imageUrl);
        MedicalRecord saved = medicalRecordRepository.save(record);
        return convertToDTO(saved);
    }

    private MedicalRecordDTO convertToDTO(MedicalRecord medicalRecord) {
        return new MedicalRecordDTO(
                medicalRecord.getId(),
                medicalRecord.getPatient().getId(),
                medicalRecord.getDoctorId(),
                medicalRecord.getDiagnosis(),
                medicalRecord.getTreatment(),
                medicalRecord.getNotes(),
                medicalRecord.getIsArchived(),
                medicalRecord.getImageUrl(),
                medicalRecord.getCreatedAt());
    }
}
