package com.example.tournament.dto.request;

import com.example.tournament.entity.TournamentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formulaire de l'interface Thymeleaf (objet mutable pour le binding).
 */
public class TournamentForm {

    @NotBlank(message = "Le nom du tournoi est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @NotBlank(message = "Le sport est obligatoire")
    @Size(max = 60, message = "Le sport ne doit pas dépasser 60 caractères")
    private String sport;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 120, message = "Le lieu ne doit pas dépasser 120 caractères")
    private String location;

    @NotNull(message = "Le nombre maximum d'équipes est obligatoire")
    @Min(value = 2, message = "Un tournoi doit accepter au moins 2 équipes")
    @Max(value = 64, message = "Un tournoi ne peut pas accepter plus de 64 équipes")
    private Integer maxTeams;

    @NotNull(message = "Le type de tournoi est obligatoire")
    private TournamentType type;

    public TournamentRequest toRequest() {
        return new TournamentRequest(name, sport, description, startDate, endDate,
                location, maxTeams, type);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getMaxTeams() { return maxTeams; }
    public void setMaxTeams(Integer maxTeams) { this.maxTeams = maxTeams; }
    public TournamentType getType() { return type; }
    public void setType(TournamentType type) { this.type = type; }
}