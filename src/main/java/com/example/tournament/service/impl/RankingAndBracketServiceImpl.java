package com.example.tournament.service.impl;

import com.example.tournament.dto.response.BracketMatchView;
import com.example.tournament.dto.response.BracketRoundView;
import com.example.tournament.dto.response.BracketTreeView;
import com.example.tournament.dto.response.RankingResponse;
import com.example.tournament.dto.response.StandingResponse;
import com.example.tournament.dto.response.TournamentStatsResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.RankingCriterion;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentTeamRepository;
import com.example.tournament.service.RankingAndBracketService;
import com.example.tournament.util.KnockoutUtils;
import com.example.tournament.util.RankingCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingAndBracketServiceImpl implements RankingAndBracketService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    public RankingAndBracketServiceImpl(TournamentRepository tournamentRepository,
                                        MatchRepository matchRepository,
                                        TournamentTeamRepository tournamentTeamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RankingResponse ranking(Long tournamentId, List<RankingCriterion> criteria) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        List<RankingCriterion> usedCriteria = (criteria == null || criteria.isEmpty())
                ? RankingCalculator.DEFAULT_CRITERIA
                : criteria;

        List<Match> matches = matchRepository
                .findByTournamentIdOrderByRoundNumberAscScheduledAtAsc(tournamentId);
        List<RankingCalculator.TeamLine> lines =
                RankingCalculator.compute(matches, usedCriteria);

        List<StandingResponse> standings = new ArrayList<>();
        int position = 1;
        for (RankingCalculator.TeamLine line : lines) {
            standings.add(new StandingResponse(
                    position++,
                    line.teamId(),
                    line.teamName(),
                    line.played(),
                    line.wins(),
                    line.draws(),
                    line.losses(),
                    line.points(),
                    line.goalsFor(),
                    line.goalsAgainst(),
                    line.goalDifference()
            ));
        }
        return new RankingResponse(tournamentId, tournament.getName(),
                tournament.getType().name(),
                usedCriteria.stream().map(Enum::name).toList(),
                standings);
    }

    @Override
    @Transactional(readOnly = true)
    public BracketTreeView bracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));

        List<Match> matches = matchRepository
                .findByTournamentIdOrderByRoundNumberAscScheduledAtAsc(tournamentId);
        Map<Integer, List<Match>> byRound = new LinkedHashMap<>();
        for (Match match : matches) {
            byRound.computeIfAbsent(match.getRoundNumber(), k -> new ArrayList<>()).add(match);
        }

        long teamCount = tournamentTeamRepository.countByTournamentId(tournamentId);
        int bracketSize = teamCount >= 2 ? KnockoutUtils.bracketSize((int) teamCount) : 0;

        List<BracketRoundView> rounds = new ArrayList<>();
        for (Map.Entry<Integer, List<Match>> entry : byRound.entrySet()) {
            List<Match> roundMatches = entry.getValue();
            roundMatches.sort(Comparator.comparing(Match::getId));
            List<BracketMatchView> views = roundMatches.stream()
                    .map(this::toBracketView)
                    .toList();
            rounds.add(new BracketRoundView(entry.getKey(),
                    roundMatches.get(0).getPhase(), views));
        }
        return new BracketTreeView(tournamentId, tournament.getName(),
                bracketSize, (int) teamCount, rounds);
    }

    private BracketMatchView toBracketView(Match match) {
        String homeLabel = match.getHomeTeam() != null
                ? match.getHomeTeam().getName()
                : (match.getHomeSourceMatch() != null
                        ? "Vainqueur match #" + match.getHomeSourceMatch().getId()
                        : "TBD");
        String awayLabel = match.getAwayTeam() != null
                ? match.getAwayTeam().getName()
                : (match.getAwaySourceMatch() != null
                        ? "Vainqueur match #" + match.getAwaySourceMatch().getId()
                        : "TBD");
        Team winner = match.getWinner();
        return new BracketMatchView(
                match.getId(),
                homeLabel,
                awayLabel,
                match.getHomeTeam() == null ? null : match.getHomeTeam().getId(),
                match.getAwayTeam() == null ? null : match.getAwayTeam().getId(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getScheduledAt(),
                match.getStatus(),
                winner == null ? null : winner.getName(),
                winner == null ? null : winner.getId(),
                match.getPhase(),
                match.getRoundNumber()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentStatsResponse stats(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        List<Match> matches = matchRepository
                .findByTournamentIdOrderByRoundNumberAscScheduledAtAsc(tournamentId);

        long total = matches.size();
        long played = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.TERMINE).count();
        long totalGoals = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.TERMINE && m.getHomeScore() != null)
                .mapToLong(m -> m.getHomeScore() + m.getAwayScore())
                .sum();
        double average = played == 0 ? 0 : (double) totalGoals / played;
        double completion = total == 0 ? 0 : (double) played / total * 100;

        Map<Long, long[]> goalsByTeam = new LinkedHashMap<>(); // id -> [goalsFor]
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.TERMINE || match.getHomeTeam() == null) {
                continue;
            }
            goalsByTeam.computeIfAbsent(match.getHomeTeam().getId(),
                    k -> new long[1])[0] += match.getHomeScore();
            goalsByTeam.computeIfAbsent(match.getAwayTeam().getId(),
                    k -> new long[1])[0] += match.getAwayScore();
        }
        Long bestTeamId = goalsByTeam.entrySet().stream()
                .max(Comparator.comparingLong(e -> e.getValue()[0]))
                .map(Map.Entry::getKey)
                .orElse(null);
        String bestName = null;
        long bestGoals = 0;
        if (bestTeamId != null) {
            for (Match match : matches) {
                if (match.getHomeTeam() != null && match.getHomeTeam().getId().equals(bestTeamId)) {
                    bestName = match.getHomeTeam().getName();
                    break;
                }
                if (match.getAwayTeam() != null && match.getAwayTeam().getId().equals(bestTeamId)) {
                    bestName = match.getAwayTeam().getName();
                    break;
                }
            }
            bestGoals = goalsByTeam.get(bestTeamId)[0];
        }
        return new TournamentStatsResponse(tournamentId, tournament.getName(),
                total, played, total - played, totalGoals, average, completion,
                bestName, bestGoals);
    }
}