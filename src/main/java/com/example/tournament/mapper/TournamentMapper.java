package com.example.tournament.mapper;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Tournament;

public final class TournamentMapper {

    private TournamentMapper() {
    }

    public static Tournament toEntity(TournamentRequest request) {
        return toEntity(request, new Tournament());
    }

    public static Tournament toEntity(TournamentRequest request, Tournament target) {
        target.setName(request.name());
        target.setSport(request.sport());
        target.setDescription(request.description());
        target.setStartDate(request.startDate());
        target.setEndDate(request.endDate());
        target.setLocation(request.location());
        target.setMaxTeams(request.maxTeams());
        target.setType(request.type());
        return target;
    }

    public static TournamentResponse toResponse(Tournament tournament) {
        long matches = tournament.getMatches() == null ? 0 : tournament.getMatches().size();
        long completed = tournament.getMatches() == null
                ? 0 : tournament.getMatches().stream()
                        .filter(m -> m.getStatus() == MatchStatus.TERMINE)
                        .count();
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getSport(),
                tournament.getDescription(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getLocation(),
                tournament.getMaxTeams(),
                tournament.getType(),
                tournament.getStatus(),
                tournament.getRegistrations() == null ? 0 : tournament.getRegistrations().size(),
                matches,
                completed,
                tournament.getCreatedAt()
        );
    }
}