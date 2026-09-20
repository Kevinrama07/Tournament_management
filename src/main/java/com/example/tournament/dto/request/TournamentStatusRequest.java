package com.example.tournament.dto.request;

import com.example.tournament.entity.TournamentStatus;
import jakarta.validation.constraints.NotNull;

public record TournamentStatusRequest(
        @NotNull(message = "Le statut est obligatoire")
        TournamentStatus status
) {
}