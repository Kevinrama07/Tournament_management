package com.example.tournament.dto.response;

public record StandingResponse(
        int position,
        Long teamId,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,
        int points,
        int goalsFor,
        int goalsAgainst,
        int goalDifference
) {
}