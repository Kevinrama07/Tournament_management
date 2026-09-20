package com.example.tournament.dto.response;

import java.util.List;

public record BracketTreeView(
        Long tournamentId,
        String tournamentName,
        int bracketSize,
        int teamCount,
        List<BracketRoundView> rounds
) {
}