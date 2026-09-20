package com.example.tournament.dto.response;

import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;

import java.time.LocalDateTime;

public record BracketMatchView(
        Long id,
        String homeLabel,
        String awayLabel,
        Long homeTeamId,
        Long awayTeamId,
        Integer homeScore,
        Integer awayScore,
        LocalDateTime scheduledAt,
        MatchStatus status,
        String winnerLabel,
        Long winnerTeamId,
        MatchPhase phase,
        int roundNumber
) {
}