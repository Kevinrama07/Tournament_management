package com.example.tournament.mapper;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.entity.Player;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static Player toEntity(PlayerRequest request) {
        return toEntity(request, new Player());
    }

    public static Player toEntity(PlayerRequest request, Player target) {
        target.setFirstName(request.firstName());
        target.setLastName(request.lastName());
        target.setPosition(request.position());
        target.setShirtNumber(request.shirtNumber());
        return target;
    }

    public static PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getPosition(),
                player.getShirtNumber(),
                player.getTeam() == null ? null : player.getTeam().getId(),
                player.getTeam() == null ? null : player.getTeam().getName()
        );
    }
}