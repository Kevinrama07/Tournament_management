package com.example.tournament.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlayerForm {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 60, message = "Le prénom ne doit pas dépasser 60 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 60, message = "Le nom ne doit pas dépasser 60 caractères")
    private String lastName;

    @Size(max = 50, message = "Le poste ne doit pas dépasser 50 caractères")
    private String position;

    @Min(value = 0, message = "Le numéro ne peut pas être négatif")
    @Max(value = 99, message = "Le numéro ne peut pas dépasser 99")
    private Integer shirtNumber;

    public PlayerRequest toRequest() {
        return new PlayerRequest(firstName, lastName, position, shirtNumber);
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Integer getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(Integer shirtNumber) { this.shirtNumber = shirtNumber; }
}