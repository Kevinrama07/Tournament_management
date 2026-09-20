package com.example.tournament.dto.response;

import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TournamentResponse(
        Long id,
        String name,
        String sport,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String location,
        int maxTeams,
        TournamentType type,
        TournamentStatus status,
        long registeredTeamCount,
        long matchCount,
        long completedMatchCount,
        LocalDateTime createdAt
) {
}