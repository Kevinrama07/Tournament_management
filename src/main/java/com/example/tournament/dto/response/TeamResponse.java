package com.example.tournament.dto.response;

import com.example.tournament.entity.TeamStatus;

public record TeamResponse(
        Long id,
        String name,
        String logo,
        String city,
        String coach,
        TeamStatus status,
        int playerCount
) {
}