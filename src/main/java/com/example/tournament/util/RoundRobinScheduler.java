package com.example.tournament.util;

import com.example.tournament.entity.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Génère le calendrier d'un championnat (méthode de la ronde / "circle method").
 * Chaque équipe rencontre chaque autre équipe exactement une fois par phase.
 * Si le nombre d'équipes est impair, une équipe bénéficie d'un "exempt" (bye) par journée.
 */
public final class RoundRobinScheduler {

    private RoundRobinScheduler() {
    }

    public record Fixture(Team home, Team away) {
    }

    /**
     * @param teams équipes participantes (dans l'ordre de tête de série)
     * @return les journées : liste de journées, chaque journée contient ses rencontres
     */
    public static List<List<Fixture>> scheduleOneCycle(List<Team> teams) {
        List<Team> pool = new ArrayList<>(teams);
        if (pool.size() % 2 == 1) {
            pool.add(null);
        }
        int n = pool.size();
        if (n == 0) {
            return List.of();
        }
        int half = n / 2;
        List<List<Fixture>> rounds = new ArrayList<>();
        for (int r = 0; r < n - 1; r++) {
            List<Fixture> round = new ArrayList<>();
            for (int i = 0; i < half; i++) {
                Team a = pool.get(i);
                Team b = pool.get(n - 1 - i);
                if (a == null || b == null) {
                    continue;
                }
                Team home;
                Team away;
                if (i == 0) {
                    // l'équipe pivot alterne domicile/extérieur
                    if (r % 2 == 0) {
                        home = a;
                        away = b;
                    } else {
                        home = b;
                        away = a;
                    }
                } else if ((i + r) % 2 == 0) {
                    home = a;
                    away = b;
                } else {
                    home = b;
                    away = a;
                }
                round.add(new Fixture(home, away));
            }
            rounds.add(round);
            // rotation de la ronde
            rotate(pool);
        }
        return rounds;
    }

    private static void rotate(List<Team> pool) {
        int n = pool.size();
        Team last = pool.get(n - 1);
        for (int i = n - 1; i > 1; i--) {
            pool.set(i, pool.get(i - 1));
        }
        pool.set(1, last);
    }
}