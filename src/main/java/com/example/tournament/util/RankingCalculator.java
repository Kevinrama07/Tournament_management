package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.RankingCriterion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcule le classement d'un championnat à partir des matchs joués.
 * Les critères de départage sont configurables (points, différence de buts,
 * buts marqués, confrontation directe, nom...).
 */
public final class RankingCalculator {

    private RankingCalculator() {
    }

    public static final List<RankingCriterion> DEFAULT_CRITERIA = List.of(
            RankingCriterion.POINTS,
            RankingCriterion.GOAL_DIFFERENCE,
            RankingCriterion.GOALS_FOR,
            RankingCriterion.HEAD_TO_HEAD,
            RankingCriterion.NAME
    );

    public record TeamLine(Long teamId, String teamName, int played, int wins, int draws,
                           int losses, int points, int goalsFor, int goalsAgainst, int goalDifference) {
    }

    private record HeadToHead(int points, int goalsFor, int goalsAgainst) {
    }

    public static List<TeamLine> compute(List<Match> allMatches, List<RankingCriterion> criteria) {
        List<Match> finished = allMatches.stream()
                .filter(m -> m.getStatus() == MatchStatus.TERMINE)
                .filter(m -> m.getHomeTeam() != null && m.getAwayTeam() != null
                        && m.getHomeScore() != null && m.getAwayScore() != null)
                .toList();

        Map<Long, TeamLine> lines = new LinkedHashMap<>();
        Map<Long, Map<Long, HeadToHead>> h2h = new HashMap<>();

        for (Match m : finished) {
            long home = m.getHomeTeam().getId();
            long away = m.getAwayTeam().getId();
            String homeName = m.getHomeTeam().getName();
            String awayName = m.getAwayTeam().getName();
            int hs = m.getHomeScore();
            int as = m.getAwayScore();

            TeamLine h = lines.computeIfAbsent(home,
                    id -> new TeamLine(id, homeName, 0, 0, 0, 0, 0, 0, 0, 0));
            TeamLine a = lines.computeIfAbsent(away,
                    id -> new TeamLine(id, awayName, 0, 0, 0, 0, 0, 0, 0, 0));

            int hp = hs > as ? 3 : (hs == as ? 1 : 0);
            int ap = as > hs ? 3 : (hs == as ? 1 : 0);

            h = new TeamLine(home, homeName, h.played() + 1,
                    h.wins() + (hp == 3 ? 1 : 0), h.draws() + (hp == 1 ? 1 : 0),
                    h.losses() + (hp == 0 ? 1 : 0), h.points() + hp,
                    h.goalsFor() + hs, h.goalsAgainst() + as, 0);
            a = new TeamLine(away, awayName, a.played() + 1,
                    a.wins() + (ap == 3 ? 1 : 0), a.draws() + (ap == 1 ? 1 : 0),
                    a.losses() + (ap == 0 ? 1 : 0), a.points() + ap,
                    a.goalsFor() + as, a.goalsAgainst() + hs, 0);
            lines.put(home, withGd(h));
            lines.put(away, withGd(a));

            Map<Long, HeadToHead> fromHome = h2h.computeIfAbsent(home, k -> new HashMap<>());
            mapHeadToHead(fromHome, away, hp, hs, as);
            Map<Long, HeadToHead> fromAway = h2h.computeIfAbsent(away, k -> new HashMap<>());
            mapHeadToHead(fromAway, home, ap, as, hs);
        }

        List<TeamLine> result = new ArrayList<>(lines.values());
        sortGroup(result, 0, criteria, h2h);
        return result;
    }

    private static TeamLine withGd(TeamLine line) {
        return new TeamLine(line.teamId(), line.teamName(), line.played(), line.wins(), line.draws(),
                line.losses(), line.points(), line.goalsFor(), line.goalsAgainst(),
                line.goalsFor() - line.goalsAgainst());
    }

    private static void mapHeadToHead(Map<Long, HeadToHead> map, long opponent, int points,
                                      int goalsFor, int goalsAgainst) {
        HeadToHead prev = map.getOrDefault(opponent, new HeadToHead(0, 0, 0));
        map.put(opponent, new HeadToHead(prev.points() + points, prev.goalsFor() + goalsFor,
                prev.goalsAgainst() + goalsAgainst));
    }

    private static void sortGroup(List<TeamLine> group, int idx, List<RankingCriterion> criteria,
                                  Map<Long, Map<Long, HeadToHead>> h2h) {
        if (group.size() <= 1) {
            return;
        }
        if (idx >= criteria.size()) {
            group.sort(Comparator.comparing(TeamLine::teamName));
            return;
        }
        RankingCriterion criterion = criteria.get(idx);
        if (criterion == RankingCriterion.NAME) {
            group.sort(Comparator.comparing(TeamLine::teamName));
            return;
        }
        Map<Long, Integer> key = new HashMap<>();
        for (TeamLine line : group) {
            key.put(line.teamId(), keyFor(line, criterion, group, h2h));
        }
        group.sort((x, y) -> Integer.compare(key.getOrDefault(y.teamId(), 0),
                key.getOrDefault(x.teamId(), 0)));
        int i = 0;
        while (i < group.size()) {
            int j = i;
            int current = key.getOrDefault(group.get(i).teamId(), 0);
            while (j + 1 < group.size() && key.getOrDefault(group.get(j + 1).teamId(), 0) == current) {
                j++;
            }
            if (j > i) {
                List<TeamLine> tied = new ArrayList<>(group.subList(i, j + 1));
                sortGroup(tied, idx + 1, criteria, h2h);
                for (int k = 0; k < tied.size(); k++) {
                    group.set(i + k, tied.get(k));
                }
            }
            i = j + 1;
        }
    }

    private static int keyFor(TeamLine line, RankingCriterion criterion, List<TeamLine> group,
                              Map<Long, Map<Long, HeadToHead>> h2h) {
        return switch (criterion) {
            case POINTS -> line.points();
            case GOAL_DIFFERENCE -> line.goalDifference();
            case GOALS_FOR -> line.goalsFor();
            case GOALS_AGAINST -> -line.goalsAgainst();
            case HEAD_TO_HEAD -> {
                int points = 0;
                Map<Long, HeadToHead> mine = h2h.getOrDefault(line.teamId(), Map.of());
                for (TeamLine other : group) {
                    if (!other.teamId().equals(line.teamId())) {
                        points += mine.getOrDefault(other.teamId(), new HeadToHead(0, 0, 0)).points();
                    }
                }
                yield points;
            }
            case NAME -> 0;
        };
    }
}