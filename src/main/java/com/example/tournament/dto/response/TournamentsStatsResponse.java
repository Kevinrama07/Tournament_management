package com.example.tournament.dto.response;

public record TournamentsStatsResponse(
        long totalTournaments,
        long teamsManaged,
        long playersManaged,
        long activeTournaments,
        long completedTournaments,
        long registeredTeamsTotal
) {
}