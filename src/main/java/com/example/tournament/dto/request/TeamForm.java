package com.example.tournament.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeamForm {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "L'URL du logo ne doit pas dépasser 255 caractères")
    private String logo;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 120, message = "La ville ne doit pas dépasser 120 caractères")
    private String city;

    @Size(max = 100, message = "Le nom de l'entraîneur ne doit pas dépasser 100 caractères")
    private String coach;

    public TeamRequest toRequest() {
        return new TeamRequest(name, logo, city, coach);
    }

    public void from(com.example.tournament.dto.response.TeamResponse response) {
        if (response == null) {
            return;
        }
        this.name = response.name();
        this.logo = response.logo();
        this.city = response.city();
        this.coach = response.coach();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }
}