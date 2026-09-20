package com.example.tournament.dto.response;

import java.util.List;

public record RankingResponse(
        Long tournamentId,
        String tournamentName,
        String tournamentType,
        List<String> criteria,
        List<StandingResponse> standings
) {
}