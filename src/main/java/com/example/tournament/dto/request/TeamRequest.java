package com.example.tournament.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank(message = "Le nom de l'équipe est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String name,

        @Size(max = 255, message = "L'URL du logo ne doit pas dépasser 255 caractères")
        String logo,

        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 120, message = "La ville ne doit pas dépasser 120 caractères")
        String city,

        @Size(max = 100, message = "Le nom de l'entraîneur ne doit pas dépasser 100 caractères")
        String coach
) {
}