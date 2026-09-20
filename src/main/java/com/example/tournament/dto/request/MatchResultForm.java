package com.example.tournament.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MatchResultForm {

    @NotNull(message = "Le score de l'équipe à domicile est obligatoire")
    @Min(value = 0, message = "Le score ne peut pas être négatif")
    private Integer homeScore;

    @NotNull(message = "Le score de l'équipe visiteuse est obligatoire")
    @Min(value = 0, message = "Le score ne peut pas être négatif")
    private Integer awayScore;

    public MatchResultRequest toRequest() {
        return new MatchResultRequest(homeScore, awayScore);
    }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
}