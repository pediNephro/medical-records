package esprit.microservice2.services;

import esprit.microservice2.dto.PatientDTO;
import java.util.List;
import java.util.Optional;

public interface IPatientService {
    PatientDTO createPatient(PatientDTO patientDTO);

    PatientDTO updatePatient(Long id, PatientDTO patientDTO);

    void deletePatient(Long id);

    Optional<PatientDTO> getPatientById(Long id);

    List<PatientDTO> getAllPatients();
}
