package com.example.tournament.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MatchResultRequest(
        @NotNull(message = "Le score de l'équipe à domicile est obligatoire")
        @PositiveOrZero(message = "Le score ne peut pas être négatif")
        Integer homeScore,

        @NotNull(message = "Le score de l'équipe visiteuse est obligatoire")
        @PositiveOrZero(message = "Le score ne peut pas être négatif")
        Integer awayScore
) {
}