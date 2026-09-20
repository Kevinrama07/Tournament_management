package com.example.tournament.dto.response;

import com.example.tournament.entity.MatchPhase;

import java.util.List;

public record BracketRoundView(
        int roundNumber,
        MatchPhase phase,
        List<BracketMatchView> matches
) {
}