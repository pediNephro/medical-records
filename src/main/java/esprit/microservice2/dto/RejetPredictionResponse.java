package esprit.microservice2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejetPredictionResponse {

    @JsonProperty("risque_rejet_pct")
    private Double risqueRejetPct;

    @JsonProperty("classe_risque")
    private String classeRisque;

    @JsonProperty("facteurs_cles")
    private List<Map<String, Object>> facteursCles;

    @JsonProperty("alerte")
    private String alerte;

    @JsonProperty("modele_utilise")
    private String modeleUtilise;
}
