package com.example.tournament.service.impl;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentTeamRepository;
import com.example.tournament.service.MatchGenerationService;
import com.example.tournament.util.KnockoutUtils;
import com.example.tournament.util.RoundRobinScheduler;
import com.example.tournament.util.TournamentStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MatchGenerationServiceImpl implements MatchGenerationService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final MatchRepository matchRepository;

    public MatchGenerationServiceImpl(TournamentRepository tournamentRepository,
                                      TournamentTeamRepository tournamentTeamRepository,
                                      MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional
    public void generate(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        TournamentStateMachine.assertGenerationOpen(tournament.getStatus());

        List<Team> teams = registeredTeamsSortedBySeed(tournamentId);
        if (teams.size() < 2) {
            throw new InvalidOperationException(
                    "Impossible de générer le calendrier : au moins 2 équipes doivent être inscrites.");
        }
        if (teams.size() > tournament.getMaxTeams()) {
            throw new InvalidOperationException(
                    "Trop d'équipes pour le nombre maximum autorisé ("
                            + tournament.getMaxTeams() + ").");
        }
        if (matchRepository.existsByTournamentId(tournamentId)) {
            throw new ConflictException("Le calendrier de ce tournoi a déjà été généré.");
        }

        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
            generateKnockoutFirstRound(tournament, teams);
        } else {
            boolean allerRetour = tournament.getType() == TournamentType.CHAMPIONNAT_ALLER_RETOUR;
            generateChampionship(tournament, teams, allerRetour);
        }
    }

    // ------------------------------------------------------------------
    // Championnat (méthode de la ronde)
    // ------------------------------------------------------------------
    private void generateChampionship(Tournament tournament, List<Team> teams,
                                      boolean allerRetour) {
        List<List<RoundRobinScheduler.Fixture>> firstCycle =
                RoundRobinScheduler.scheduleOneCycle(teams);
        List<List<RoundRobinScheduler.Fixture>> allRounds =
                new ArrayList<>(firstCycle);
        if (allerRetour) {
            List<List<RoundRobinScheduler.Fixture>> returnCycle = new ArrayList<>();
            for (int i = firstCycle.size() - 1; i >= 0; i--) {
                List<RoundRobinScheduler.Fixture> mirrored = new ArrayList<>();
                for (RoundRobinScheduler.Fixture fixture : firstCycle.get(i)) {
                    mirrored.add(new RoundRobinScheduler.Fixture(fixture.away(), fixture.home()));
                }
                returnCycle.add(mirrored);
            }
            allRounds.addAll(returnCycle);
        }

        int totalRounds = allRounds.size();
        List<Match> matches = new ArrayList<>();
        int roundNumber = 1;
        for (List<RoundRobinScheduler.Fixture> round : allRounds) {
            int slot = 0;
            LocalDate day = roundSpreadDay(tournament, roundNumber - 1, totalRounds);
            for (RoundRobinScheduler.Fixture fixture : round) {
                matches.add(buildMatch(tournament, fixture.home(), fixture.away(),
                        MatchPhase.REGULIER, roundNumber, day, slot++));
            }
            roundNumber++;
        }
        matchRepository.saveAll(matches);
    }

    // ------------------------------------------------------------------
    // Élimination directe
    // ------------------------------------------------------------------
    private void generateKnockoutFirstRound(Tournament tournament, List<Team> teams) {
        int n = teams.size();
        int m = KnockoutUtils.bracketSize(n);
        int byes = m - n;
        Map<Integer, Team> byeSlots = computeByeSlots(n, teams);

        List<Team> playing = teams.subList(byes, n);
        List<Match> matches = new ArrayList<>();
        int totalRounds = KnockoutUtils.numberOfRounds(m);
        int playerIndex = 0;
        int assigned = 0;
        for (int slot = 0; slot < m / 2 && playerIndex < playing.size(); slot++) {
            if (byeSlots.containsKey(slot)) {
                continue;
            }
            Team home = playing.get(playerIndex++);
            Team away = playing.get(playerIndex++);
            matches.add(buildMatch(tournament, home, away, phaseForRound(1, m), 1,
                    roundSpreadDay(tournament, 0, totalRounds), assigned++));
        }
        matchRepository.saveAll(matches);
    }

    @Override
    @Transactional
    public void advanceAfterRound(Tournament tournament, int completedRound) {
        int teamCount = (int) tournamentTeamRepository.countByTournamentId(tournament.getId());
        int m = KnockoutUtils.bracketSize(teamCount);
        int totalRounds = KnockoutUtils.numberOfRounds(m);

        if (completedRound >= totalRounds) {
            tournament.setStatus(TournamentStatus.TERMINE);
            tournamentRepository.save(tournament);
            return;
        }

        List<Match> prevRound = matchRepository
                .findByTournamentIdAndRoundNumberOrderByIdAsc(tournament.getId(), completedRound);
        boolean allFinished = !prevRound.isEmpty() && prevRound.stream()
                .allMatch(mt -> mt.getStatus() == MatchStatus.TERMINE);
        if (!allFinished) {
            return;
        }

        List<Team> participants = new ArrayList<>();
        if (completedRound == 1) {
            List<Team> actualTeams = registeredTeamsSortedBySeed(tournament.getId());
            Map<Integer, Team> byeSlots = computeByeSlots(teamCount, actualTeams);
            int playIndex = 0;
            for (int slot = 0; slot < m / 2; slot++) {
                if (byeSlots.containsKey(slot)) {
                    participants.add(byeSlots.get(slot));
                } else {
                    participants.add(prevRound.get(playIndex++).getWinner());
                }
            }
        } else {
            for (Match match : prevRound) {
                participants.add(match.getWinner());
            }
        }

        int nextRound = completedRound + 1;
        MatchPhase phase = phaseForRound(nextRound, m);
        List<Match> nextMatches = new ArrayList<>();
        for (int i = 0; i < participants.size(); i += 2) {
            Team home = participants.get(i);
            Team away = participants.get(i + 1);
            nextMatches.add(buildKnockoutMatch(tournament, home, away, prevRound,
                    i / 2, nextRound, phase,
                    roundSpreadDay(tournament, nextRound - 1, totalRounds)));
        }
        matchRepository.saveAll(nextMatches);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private List<Team> registeredTeamsSortedBySeed(Long tournamentId) {
        return tournamentTeamRepository.findByTournamentIdOrderBySeedAsc(tournamentId)
                .stream()
                .map(reg -> reg.getTeam())
                .toList();
    }

    /**
     * Calcule, pour le premier tour, les slots "exempts" (byes) : ils sont répartis
     * uniformément sur le tableau et reviennent aux meilleures têtes de série.
     */
    private Map<Integer, Team> computeByeSlots(int teamCount, List<Team> teams) {
        Map<Integer, Team> map = new TreeMap<>();
        int m = KnockoutUtils.bracketSize(teamCount);
        int byes = m - teamCount;
        if (byes <= 0) {
            return map;
        }
        int totalSlots = m / 2;
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            slots.add((i * totalSlots) / byes);
        }
        slots.sort(Comparator.naturalOrder());
        for (int i = 0; i < byes; i++) {
            map.put(slots.get(i), teams.get(i));
        }
        return map;
    }

    private Match buildMatch(Tournament tournament, Team home, Team away, MatchPhase phase,
                             int roundNumber, LocalDate day, int slot) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setPhase(phase);
        match.setRoundNumber(roundNumber);
        match.setScheduledAt(day.atTime(hourForSlot(slot), 0));
        match.setVenue(tournament.getLocation() + " - Terrain " + (slot % 4 + 1));
        match.setStatus(MatchStatus.PROGRAMME);
        return match;
    }

    private Match buildKnockoutMatch(Tournament tournament, Team home, Team away,
                                     List<Match> previousRound, int index, int roundNumber,
                                     MatchPhase phase, LocalDate day) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        if (previousRound != null) {
            int homeSource = index * 2;
            int awaySource = index * 2 + 1;
            if (homeSource < previousRound.size()) {
                match.setHomeSourceMatch(previousRound.get(homeSource));
            }
            if (awaySource < previousRound.size()) {
                match.setAwaySourceMatch(previousRound.get(awaySource));
            }
        }
        match.setPhase(phase);
        match.setRoundNumber(roundNumber);
        match.setScheduledAt(day.atTime(hourForSlot(index), 0));
        match.setVenue(tournament.getLocation() + " - Terrain " + (index % 4 + 1));
        match.setStatus(MatchStatus.PROGRAMME);
        return match;
    }

    private int hourForSlot(int slot) {
        return 14 + (slot % 4) * 2;
    }

    private LocalDate roundSpreadDay(Tournament tournament, int roundIndex, int totalRounds) {
        LocalDate start = tournament.getStartDate();
        LocalDate end = tournament.getEndDate();
        if (totalRounds <= 1) {
            return start;
        }
        long span = ChronoUnit.DAYS.between(start, end);
        long step = Math.max(0, span / (totalRounds - 1));
        LocalDate day = start.plusDays(step * roundIndex);
        return day.isAfter(end) ? end : day;
    }

    private MatchPhase phaseForRound(int roundNumber, int bracketSize) {
        int roundsTotal = KnockoutUtils.numberOfRounds(bracketSize);
        int fromTop = roundsTotal - roundNumber;
        return switch (fromTop) {
            case 0 -> MatchPhase.FINALE;
            case 1 -> MatchPhase.DEMI_FINALE;
            case 2 -> MatchPhase.QUARTS;
            case 3 -> MatchPhase.HUITIEMES;
            case 4 -> MatchPhase.SEIZIEMES;
            default -> MatchPhase.TOUR_PRELIMINAIRE;
        };
    }
}