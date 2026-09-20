package com.example.tournament.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TournamentTeamRequest(
        @NotNull(message = "L'identifiant de l'équipe est obligatoire")
        @Positive(message = "L'identifiant de l'équipe doit être positif")
        Long teamId
) {
}