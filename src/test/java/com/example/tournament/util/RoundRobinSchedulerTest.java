package com.example.tournament.util;

import com.example.tournament.entity.Team;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RoundRobinSchedulerTest {

    private Team team(long id, String name) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setCity("City");
        return t;
    }

    private List<Team> teams(long... ids) {
        return java.util.Arrays.stream(ids)
                .mapToObj(id -> team(id, "Equipe " + id))
                .toList();
    }

    @Test
    void fourTeamsSingleCycle_producesSixMatchesInThreeRounds() {
        List<List<RoundRobinScheduler.Fixture>> rounds =
                RoundRobinScheduler.scheduleOneCycle(teams(1, 2, 3, 4));

        assertThat(rounds).hasSize(3);
        long total = rounds.stream().mapToLong(List::size).sum();
        assertThat(total).isEqualTo(6);

        Map<String, Long> pairs = rounds.stream()
                .flatMap(List::stream)
                .map(f -> key(f.home().getId(), f.away().getId()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        // chaque paire exactement une fois
        assertThat(pairs).hasSize(6);
        assertThat(pairs.values()).allMatch(v -> v == 1L);
    }

    @Test
    void fourTeams_everyTeamPlaysEveryRound() {
        List<List<RoundRobinScheduler.Fixture>> rounds =
                RoundRobinScheduler.scheduleOneCycle(teams(1, 2, 3, 4));

        for (List<RoundRobinScheduler.Fixture> round : rounds) {
            assertThat(round).hasSize(2);
            List<Long> ids = round.stream().flatMap(f -> List.of(f.home().getId(), f.away().getId()).stream()).toList();
            assertThat(ids).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        }
    }

    @Test
    void oddNumberOfTeams_producesOneByePerRound() {
        List<List<RoundRobinScheduler.Fixture>> rounds =
                RoundRobinScheduler.scheduleOneCycle(teams(1, 2, 3, 4, 5));

        // 5 équipes → 5 journées, 2 matchs chacune (1 exempt)
        assertThat(rounds).hasSize(5);
        assertThat(rounds).allMatch(r -> r.size() == 2);

        long total = rounds.stream().mapToLong(List::size).sum();
        assertThat(total).isEqualTo(10);

        Map<String, Long> pairs = rounds.stream()
                .flatMap(List::stream)
                .map(f -> key(f.home().getId(), f.away().getId()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertThat(pairs).hasSize(10);
    }

    @Test
    void noTeamPlaysItself() {
        List<List<RoundRobinScheduler.Fixture>> rounds =
                RoundRobinScheduler.scheduleOneCycle(teams(1, 2, 3, 4, 5, 6));

        for (List<RoundRobinScheduler.Fixture> round : rounds) {
            for (RoundRobinScheduler.Fixture f : round) {
                assertThat(f.home().getId()).isNotEqualTo(f.away().getId());
            }
        }
    }

    @Test
    void emptyList_returnsEmptyRounds() {
        assertThat(RoundRobinScheduler.scheduleOneCycle(List.of())).isEmpty();
    }

    @Test
    void twoTeams_singleMatch() {
        List<List<RoundRobinScheduler.Fixture>> rounds = RoundRobinScheduler.scheduleOneCycle(teams(1, 2));
        assertThat(rounds).hasSize(1);
        assertThat(rounds.get(0)).hasSize(1);
    }

    private String key(Long home, Long away) {
        return Math.min(home, away) + "-" + Math.max(home, away);
    }
}