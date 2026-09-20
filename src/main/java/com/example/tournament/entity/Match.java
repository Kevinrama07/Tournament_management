package com.example.tournament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches",
        uniqueConstraints = @UniqueConstraint(name = "uk_tournament_round_home_away",
                columnNames = {"tournament_id", "round_number", "home_team_id", "away_team_id"}))
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_source_match_id")
    private Match homeSourceMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_source_match_id")
    private Match awaySourceMatch;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 120)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MatchPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.PROGRAMME;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "result_recorded_at")
    private LocalDateTime resultRecordedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    public Match getHomeSourceMatch() { return homeSourceMatch; }
    public void setHomeSourceMatch(Match homeSourceMatch) { this.homeSourceMatch = homeSourceMatch; }
    public Match getAwaySourceMatch() { return awaySourceMatch; }
    public void setAwaySourceMatch(Match awaySourceMatch) { this.awaySourceMatch = awaySourceMatch; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public MatchPhase getPhase() { return phase; }
    public void setPhase(MatchPhase phase) { this.phase = phase; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
    public LocalDateTime getResultRecordedAt() { return resultRecordedAt; }
    public void setResultRecordedAt(LocalDateTime resultRecordedAt) { this.resultRecordedAt = resultRecordedAt; }

    public Team getWinner() {
        if (status != MatchStatus.TERMINE || homeScore == null || awayScore == null) {
            return null;
        }
        if (homeScore.equals(awayScore)) {
            return null;
        }
        return homeScore > awayScore ? homeTeam : awayTeam;
    }
}