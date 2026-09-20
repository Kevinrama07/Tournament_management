package com.example.tournament.dto.response;

import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;

import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        Long tournamentId,
        String tournamentName,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        Long homeSourceMatchId,
        Long awaySourceMatchId,
        LocalDateTime scheduledAt,
        String venue,
        MatchPhase phase,
        int roundNumber,
        MatchStatus status,
        Integer homeScore,
        Integer awayScore,
        Long winnerTeamId,
        String winnerTeamName
) {
}