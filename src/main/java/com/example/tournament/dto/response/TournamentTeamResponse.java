package com.example.tournament.dto.response;

import java.time.LocalDateTime;

public record TournamentTeamResponse(
        Long id,
        Long tournamentId,
        Long teamId,
        String teamName,
        String teamLogo,
        String teamCity,
        LocalDateTime registrationDate,
        Integer seed
) {
}