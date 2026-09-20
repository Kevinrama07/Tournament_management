package com.example.tournament.dto.response;

public record TournamentStatsResponse(
        Long tournamentId,
        String tournamentName,
        long totalMatches,
        long playedMatches,
        long remainingMatches,
        long totalGoals,
        double averageGoalsPerMatch,
        double completionRate,
        String bestTeamName,
        long bestTeamGoals
) {
}