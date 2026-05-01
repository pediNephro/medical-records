package esprit.microservice2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejetPredictionRequest {

    @JsonProperty("creatinine_umoll")
    private Double creatinineUmoll;

    @JsonProperty("uree_mmoll")
    private Double ureeMmoll;

    @JsonProperty("proteinurie_24h")
    private Double proteinurie24h;

    @JsonProperty("tacrolimus_taux_ngml")
    private Double tacrolimusTauxNgml;

    @JsonProperty("pression_arterielle_sys")
    private Double pressionArterielleSys;

    @JsonProperty("pression_arterielle_dia")
    private Double pressionArterielleDia;

    @JsonProperty("score_banff")
    private Integer scoreBanff;

    @JsonProperty("age_mois")
    private Integer ageMois;

    @JsonProperty("poids_kg")
    private Double poidsKg;

    @JsonProperty("jours_post_greffe")
    private Integer joursPostGreffe;

    @JsonProperty("dfg_ml_min")
    private Double dfgMlMin;

    @JsonProperty("hemoglobine_gdl")
    private Double hemoglobineGdl;

    @JsonProperty("crp_mgl")
    private Double crpMgl;

    @JsonProperty("variation_creatinine")
    private Double variationCreatinine;
}
