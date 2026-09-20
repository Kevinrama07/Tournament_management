package com.example.tournament.dto.response;

public record PlayerResponse(
        Long id,
        String firstName,
        String lastName,
        String position,
        Integer shirtNumber,
        Long teamId,
        String teamName
) {
}