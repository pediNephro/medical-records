package esprit.microservice2.services;

import esprit.microservice2.dto.RejetPredictionRequest;
import esprit.microservice2.dto.RejetPredictionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejetMLService - Tests unitaires")
class RejetMLServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private RejetMLService rejetMLService;

    private static final String ML_API_URL = "http://localhost:5000";

    @BeforeEach
    void setUp() {
        // Inject mock RestTemplate via reflection since the service creates its own
        rejetMLService = new RejetMLService(ML_API_URL) {
            {
                // Override via field injection using reflection
                try {
                    java.lang.reflect.Field field =
                            RejetMLService.class.getDeclaredField("restTemplate");
                    field.setAccessible(true);
                    field.set(this, restTemplate);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private RejetPredictionRequest buildRequest() {
        RejetPredictionRequest req = new RejetPredictionRequest();
        req.setCreatinineUmoll(350.0);
        req.setUreeMmoll(18.5);
        req.setProteinurie24h(1.2);
        req.setTacrolimusTauxNgml(8.0);
        req.setPressionArterielleSys(130.0);
        req.setPressionArterielleDia(85.0);
        req.setScoreBanff(2);
        req.setAgeMois(72);
        req.setPoidsKg(22.0);
        req.setJoursPostGreffe(180);
        req.setDfgMlMin(45.0);
        req.setHemoglobineGdl(11.5);
        req.setCrpMgl(12.0);
        req.setVariationCreatinine(15.0);
        return req;
    }

    // ─── predireRisqueRejet ───────────────────────────────────────────────────

    @Test
    @DisplayName("predireRisqueRejet - retourne la prédiction du modèle ML")
    void predireRisqueRejet_success_returnsPrediction() {
        RejetPredictionResponse expected = new RejetPredictionResponse();
        expected.setRisqueRejetPct(72.5);
        expected.setClasseRisque("ÉLEVÉ");
        expected.setAlerte("Risque élevé de rejet — consultation urgente recommandée");
        expected.setModeleUtilise("RandomForestClassifier");
        expected.setFacteursCles(List.of(Map.of("feature", "creatinine", "importance", 0.35)));

        when(restTemplate.postForObject(
                eq(ML_API_URL + "/predict/rejet"),
                any(HttpEntity.class),
                eq(RejetPredictionResponse.class)
        )).thenReturn(expected);

        RejetPredictionResponse result = rejetMLService.predireRisqueRejet(buildRequest());

        assertThat(result).isNotNull();
        assertThat(result.getRisqueRejetPct()).isEqualTo(72.5);
        assertThat(result.getClasseRisque()).isEqualTo("ÉLEVÉ");
        assertThat(result.getAlerte()).contains("Risque élevé");
        assertThat(result.getModeleUtilise()).isEqualTo("RandomForestClassifier");
        assertThat(result.getFacteursCles()).hasSize(1);
    }

    @Test
    @DisplayName("predireRisqueRejet - retourne null si API ML indisponible")
    void predireRisqueRejet_mlUnavailable_returnsNull() {
        when(restTemplate.postForObject(
                eq(ML_API_URL + "/predict/rejet"),
                any(HttpEntity.class),
                eq(RejetPredictionResponse.class)
        )).thenReturn(null);

        RejetPredictionResponse result = rejetMLService.predireRisqueRejet(buildRequest());

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("predireRisqueRejet - propage l'exception si le serveur ML est inaccessible")
    void predireRisqueRejet_connectionError_throwsException() {
        when(restTemplate.postForObject(
                eq(ML_API_URL + "/predict/rejet"),
                any(HttpEntity.class),
                eq(RejetPredictionResponse.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> rejetMLService.predireRisqueRejet(buildRequest()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Connection refused");
    }

    @Test
    @DisplayName("predireRisqueRejet - envoie la requête au bon endpoint")
    void predireRisqueRejet_callsCorrectEndpoint() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(new RejetPredictionResponse());

        rejetMLService.predireRisqueRejet(buildRequest());

        verify(restTemplate).postForObject(
                eq(ML_API_URL + "/predict/rejet"),
                any(HttpEntity.class),
                eq(RejetPredictionResponse.class));
    }

    // ─── getModelMetrics ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getModelMetrics - retourne les métriques du modèle")
    void getModelMetrics_success_returnsMetrics() {
        Map<String, Object> metrics = Map.of(
                "accuracy", 0.89,
                "precision", 0.87,
                "recall", 0.91,
                "f1_score", 0.89);

        when(restTemplate.getForObject(ML_API_URL + "/metrics", Object.class))
                .thenReturn(metrics);

        Object result = rejetMLService.getModelMetrics();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Map.class);
        verify(restTemplate).getForObject(ML_API_URL + "/metrics", Object.class);
    }

    @Test
    @DisplayName("getModelMetrics - retourne null si API indisponible")
    void getModelMetrics_unavailable_returnsNull() {
        when(restTemplate.getForObject(ML_API_URL + "/metrics", Object.class))
                .thenReturn(null);

        Object result = rejetMLService.getModelMetrics();

        assertThat(result).isNull();
    }

    // ─── getHealthStatus ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getHealthStatus - retourne le statut de santé du service ML")
    void getHealthStatus_success_returnsStatus() {
        Map<String, Object> health = Map.of("status", "healthy", "model_loaded", true);

        when(restTemplate.getForObject(ML_API_URL + "/health", Object.class))
                .thenReturn(health);

        Object result = rejetMLService.getHealthStatus();

        assertThat(result).isNotNull();
        verify(restTemplate).getForObject(ML_API_URL + "/health", Object.class);
    }

    @Test
    @DisplayName("getHealthStatus - appelle le bon endpoint /health")
    void getHealthStatus_callsCorrectEndpoint() {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);

        rejetMLService.getHealthStatus();

        verify(restTemplate).getForObject(ML_API_URL + "/health", Object.class);
    }
}
