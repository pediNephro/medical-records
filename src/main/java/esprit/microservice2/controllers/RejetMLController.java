package esprit.microservice2.controllers;

import esprit.microservice2.dto.RejetPredictionRequest;
import esprit.microservice2.dto.RejetPredictionResponse;
import esprit.microservice2.services.RejetMLService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ml/rejet")
@RequiredArgsConstructor
public class RejetMLController {

    private final RejetMLService rejetMLService;

    @PostMapping("/predict")
    public ResponseEntity<?> predictRejection(@RequestBody RejetPredictionRequest request) {
        try {
            RejetPredictionResponse response = rejetMLService.predireRisqueRejet(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "ML service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        try {
            return ResponseEntity.ok(rejetMLService.getModelMetrics());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "ML service unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        try {
            return ResponseEntity.ok(rejetMLService.getHealthStatus());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "ML service unavailable: " + e.getMessage()));
        }
    }
}
