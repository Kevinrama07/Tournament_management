package com.example.tournament.mapper;

import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.entity.Team;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static Team toEntity(TeamRequest request) {
        return toEntity(request, new Team());
    }

    public static Team toEntity(TeamRequest request, Team target) {
        target.setName(request.name());
        target.setLogo(request.logo());
        target.setCity(request.city());
        target.setCoach(request.coach());
        return target;
    }

    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getLogo(),
                team.getCity(),
                team.getCoach(),
                team.getStatus(),
                team.getPlayers() == null ? 0 : team.getPlayers().size()
        );
    }
}