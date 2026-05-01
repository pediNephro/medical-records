package esprit.microservice2.controllers;

import esprit.microservice2.dto.PatientDTO;
import esprit.microservice2.services.IPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO) {
        PatientDTO createdPatient = patientService.createPatient(patientDTO);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        Optional<PatientDTO> patient = patientService.getPatientById(id);
        if (patient.isPresent()) {
            return new ResponseEntity<>(patient.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @RequestBody PatientDTO patientDTO) {
        PatientDTO updatedPatient = patientService.updatePatient(id, patientDTO);
        if (updatedPatient != null) {
            return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
