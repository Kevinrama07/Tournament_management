package com.example.tournament.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(name = "uk_team_name", columnNames = "name"))
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String logo;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(length = 100)
    private String coach;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamStatus status = TeamStatus.ACTIF;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<TournamentTeam> tournaments = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
        player.setTeam(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setTeam(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }
    public TeamStatus getStatus() { return status; }
    public void setStatus(TeamStatus status) { this.status = status; }
    public List<Player> getPlayers() { return players; }
    public List<TournamentTeam> getTournaments() { return tournaments; }
}