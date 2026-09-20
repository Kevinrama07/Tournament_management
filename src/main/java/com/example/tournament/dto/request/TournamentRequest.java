package com.example.tournament.dto.request;

import com.example.tournament.entity.TournamentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TournamentRequest(
        @NotBlank(message = "Le nom du tournoi est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String name,

        @NotBlank(message = "Le sport est obligatoire")
        @Size(max = 60, message = "Le sport ne doit pas dépasser 60 caractères")
        String sport,

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate startDate,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDate endDate,

        @NotBlank(message = "Le lieu est obligatoire")
        @Size(max = 120, message = "Le lieu ne doit pas dépasser 120 caractères")
        String location,

        @NotNull(message = "Le nombre maximum d'équipes est obligatoire")
        @Min(value = 2, message = "Un tournoi doit accepter au moins 2 équipes")
        @Max(value = 64, message = "Un tournoi ne peut pas accepter plus de 64 équipes")
        Integer maxTeams,

        @NotNull(message = "Le type de tournoi est obligatoire")
        TournamentType type
) {
}