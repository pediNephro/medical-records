package esprit.microservice2.services;

import esprit.microservice2.dto.RejetPredictionRequest;
import esprit.microservice2.dto.RejetPredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RejetMLService {

    private final RestTemplate restTemplate;
    private final String mlApiUrl;

    public RejetMLService(@Value("${ml.rejet.api.url}") String mlApiUrl) {
        this.restTemplate = new RestTemplate();
        this.mlApiUrl = mlApiUrl;
    }

    public RejetPredictionResponse predireRisqueRejet(RejetPredictionRequest request) {
        log.info("Calling ML API for rejection prediction...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RejetPredictionRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(mlApiUrl + "/predict/rejet", entity, RejetPredictionResponse.class);
    }

    public Object getModelMetrics() {
        return restTemplate.getForObject(mlApiUrl + "/metrics", Object.class);
    }

    public Object getHealthStatus() {
        return restTemplate.getForObject(mlApiUrl + "/health", Object.class);
    }
}
