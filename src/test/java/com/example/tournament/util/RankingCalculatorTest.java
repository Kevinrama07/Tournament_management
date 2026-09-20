package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.RankingCriterion;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.util.RankingCalculator.TeamLine;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankingCalculatorTest {

    private Team team(long id, String name) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setCity("Ville");
        return t;
    }

    private Match finished(Tournament t, Team home, Team away, int hs, int as) {
        Match m = new Match();
        m.setTournament(t);
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setHomeScore(hs);
        m.setAwayScore(as);
        m.setStatus(MatchStatus.TERMINE);
        m.setPhase(MatchPhase.REGULIER);
        m.setRoundNumber(1);
        m.setScheduledAt(LocalDateTime.now());
        return m;
    }

    @Test
    void compute_sortsByPointsThenGoalDifference() {
        Team a = team(1, "Lyon");
        Team b = team(2, "Paris");
        Team c = team(3, "Marseille");
        Tournament t = new Tournament();

        // A gagne 2-0 vs B, fait nul 1-1 vs C → 4 pts, +2
        // B gagne 3-0 vs C, perd 0-2 vs A → 3 pts, +1
        // C nul 1-1 vs A, perd 0-3 vs B → 1 pt, -3
        List<Match> matches = List.of(
                finished(t, a, b, 2, 0),
                finished(t, a, c, 1, 1),
                finished(t, b, c, 3, 0));

        List<TeamLine> ranking = RankingCalculator.compute(matches, RankingCalculator.DEFAULT_CRITERIA);

        assertThat(ranking).hasSize(3);
        assertThat(ranking.get(0).teamName()).isEqualTo("Lyon");
        assertThat(ranking.get(0).points()).isEqualTo(4);
        assertThat(ranking.get(1).teamName()).isEqualTo("Paris");
        assertThat(ranking.get(1).points()).isEqualTo(3);
        assertThat(ranking.get(2).teamName()).isEqualTo("Marseille");
        assertThat(ranking.get(2).points()).isEqualTo(1);
    }

    @Test
    void compute_tracksPlayedWinsDrawsLossesAndGoals() {
        Team a = team(1, "A");
        Team b = team(2, "B");
        Tournament t = new Tournament();

        List<Match> matches = List.of(
                finished(t, a, b, 3, 1),
                finished(t, b, a, 1, 1));

        List<TeamLine> ranking = RankingCalculator.compute(matches, RankingCalculator.DEFAULT_CRITERIA);

        TeamLine lineA = ranking.stream().filter(l -> l.teamName().equals("A")).findFirst().orElseThrow();
        assertThat(lineA.played()).isEqualTo(2);
        assertThat(lineA.wins()).isEqualTo(1);
        assertThat(lineA.draws()).isEqualTo(1);
        assertThat(lineA.losses()).isEqualTo(0);
        assertThat(lineA.goalsFor()).isEqualTo(4);
        assertThat(lineA.goalsAgainst()).isEqualTo(2);
        assertThat(lineA.goalDifference()).isEqualTo(2);
        assertThat(lineA.points()).isEqualTo(4);
    }

    @Test
    void compute_breaksTiesByGoalsForWhenConfigured() {
        Team a = team(1, "Alpha");
        Team b = team(2, "Bravo");
        Team c = team(3, "Charlie");
        Tournament t = new Tournament();

        // A : 3 pts (1V, 0N, 1D) +3 ; B : 3 pts (1V, 0N, 1D) +3 ; C : 0 pt
        // Même nombre de points et même différence → départage par buts marqués
        List<Match> matches = List.of(
                finished(t, a, c, 5, 0),
                finished(t, c, b, 1, 2),
                finished(t, b, a, 2, 0));

        List<TeamLine> ranking = RankingCalculator.compute(matches, List.of(
                RankingCriterion.POINTS,
                RankingCriterion.GOAL_DIFFERENCE,
                RankingCriterion.GOALS_FOR,
                RankingCriterion.NAME));

        assertThat(ranking).hasSize(3);
        // A: +5-2 = +3, 5 marqués ; B: +2-2=0... recalculons
        // A bat C 5-0, perd contre B 0-2 → 3 pts, +3, 5 marqués
        // B bat C 2-1, bat A 2-0 → 6 pts
        assertThat(ranking.get(0).teamName()).isEqualTo("Bravo");
        assertThat(ranking.get(1).teamName()).isEqualTo("Alpha");
        assertThat(ranking.get(2).teamName()).isEqualTo("Charlie");
    }

    @Test
    void compute_ignoresMatchesWithoutResults() {
        Team a = team(1, "A");
        Team b = team(2, "B");

        Match scheduled = new Match();
        scheduled.setHomeTeam(a);
        scheduled.setAwayTeam(b);
        scheduled.setStatus(MatchStatus.PROGRAMME);
        scheduled.setPhase(MatchPhase.REGULIER);
        scheduled.setRoundNumber(1);
        scheduled.setScheduledAt(LocalDateTime.now());

        List<TeamLine> ranking = RankingCalculator.compute(List.of(scheduled), RankingCalculator.DEFAULT_CRITERIA);
        assertThat(ranking).isEmpty();
    }

    @Test
    void compute_emptyCandidateList() {
        assertThat(RankingCalculator.compute(List.of(), RankingCalculator.DEFAULT_CRITERIA)).isEmpty();
    }
}