package com.example.tournament.mapper;

import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.TournamentTeam;

public final class MatchMapper {

    private MatchMapper() {
    }

    public static MatchResponse toResponse(Match match) {
        Long winnerId = null;
        String winnerName = null;
        if (match.getWinner() != null) {
            winnerId = match.getWinner().getId();
            winnerName = match.getWinner().getName();
        }
        return new MatchResponse(
                match.getId(),
                match.getTournament() == null ? null : match.getTournament().getId(),
                match.getTournament() == null ? null : match.getTournament().getName(),
                match.getHomeTeam() == null ? null : match.getHomeTeam().getId(),
                match.getHomeTeam() == null ? null : match.getHomeTeam().getName(),
                match.getAwayTeam() == null ? null : match.getAwayTeam().getId(),
                match.getAwayTeam() == null ? null : match.getAwayTeam().getName(),
                match.getHomeSourceMatch() == null ? null : match.getHomeSourceMatch().getId(),
                match.getAwaySourceMatch() == null ? null : match.getAwaySourceMatch().getId(),
                match.getScheduledAt(),
                match.getVenue(),
                match.getPhase(),
                match.getRoundNumber(),
                match.getStatus(),
                match.getHomeScore(),
                match.getAwayScore(),
                winnerId,
                winnerName
        );
    }

    public static TournamentTeamResponse toResponse(TournamentTeam registration) {
        return new TournamentTeamResponse(
                registration.getId(),
                registration.getTournament() == null ? null : registration.getTournament().getId(),
                registration.getTeam() == null ? null : registration.getTeam().getId(),
                registration.getTeam() == null ? null : registration.getTeam().getName(),
                registration.getTeam() == null ? null : registration.getTeam().getLogo(),
                registration.getTeam() == null ? null : registration.getTeam().getCity(),
                registration.getRegistrationDate(),
                registration.getSeed()
        );
    }
}