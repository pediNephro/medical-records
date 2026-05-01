package esprit.microservice2.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String gender; // MALE, FEMALE, OTHER

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords;
}
