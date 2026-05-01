package esprit.microservice2.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esprit.microservice2.dto.MedicalRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyze medical record using Gemini AI REST API
     */
    public String analyzeMedicalRecord(MedicalRecordDTO record) {
        try {
            log.info("Analyzing medical record {} with Gemini model {}", record.getId(), modelName);

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    modelName, apiKey);

            String prompt = buildAnalysisPrompt(record);

            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)))));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        String analysis = parts.get(0).path("text").asText();
                        log.info("Gemini analysis completed for record {}", record.getId());
                        return analysis;
                    }
                }
            }

            log.warn("Empty response from Gemini API for record {}", record.getId());
            return "Analyse non disponible - réponse vide de l'API Gemini.";

        } catch (Exception e) {
            log.error("Error analyzing medical record with Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'analyse avec Gemini: " + e.getMessage(), e);
        }
    }

    private String buildAnalysisPrompt(MedicalRecordDTO record) {
        return String.format(
                "Tu es un assistant médical expert. Analyse le dossier médical suivant et fournis un résumé clinique complet en français:\n\n"
                        +
                        "**Diagnostic:** %s\n" +
                        "**Traitement:** %s\n" +
                        "**Notes médicales:** %s\n\n" +
                        "Fournis:\n" +
                        "1. Un résumé du diagnostic\n" +
                        "2. Une évaluation clinique du plan de traitement\n" +
                        "3. Des recommandations de suivi\n" +
                        "4. Les considérations ou alertes potentielles\n\n" +
                        "Formate la réponse de manière professionnelle pour une documentation médicale.",
                record.getDiagnosis(),
                record.getTreatment() != null ? record.getTreatment() : "Non spécifié",
                record.getNotes() != null ? record.getNotes() : "Aucune note");
    }
}
