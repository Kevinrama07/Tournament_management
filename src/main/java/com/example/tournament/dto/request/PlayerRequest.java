package com.example.tournament.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerRequest(
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 60, message = "Le prénom ne doit pas dépasser 60 caractères")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 60, message = "Le nom ne doit pas dépasser 60 caractères")
        String lastName,

        @Size(max = 50, message = "Le poste ne doit pas dépasser 50 caractères")
        String position,

        @Min(value = 0, message = "Le numéro ne peut pas être négatif")
        @Max(value = 99, message = "Le numéro ne peut pas dépasser 99")
        Integer shirtNumber
) {
}