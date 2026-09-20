package com.example.tournament.util;

/**
 * Outils liés au découpage d'un tournoi à élimination directe en phase
 * (huitièmes, quarts, demi-finales, finale...).
 */
public final class KnockoutUtils {

    private KnockoutUtils() {
    }

    /**
     * Taille du tableau (puissance de 2) qui contient les n équipes.
     */
    public static int bracketSize(int teamCount) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("teamCount must be > 0");
        }
        return Integer.highestOneBit(teamCount - 1) << 1;
    }

    /**
     * Nombre de tours au total pour un tableau de cette taille.
     */
    public static int numberOfRounds(int bracketSize) {
        return 32 - Integer.numberOfLeadingZeros(bracketSize) - 1;
    }

    /**
     * Nombre d'équipes "exemptes" (byes) au premier tour.
     */
    public static int numberPlateau(int bracketSize, int teamCount) {
        return bracketSize - teamCount;
    }

    /**
     * Nombre de matchs de la journée numéro <code>roundNumber</code> (1 = premier tour).
     */
    public static int matchesForRound(int bracketSize, int roundNumber) {
        return bracketSize >> roundNumber;
    }

    /**
     * Les phases "nobles" sont nommées à partir de la finale ; les tours plus
     * éloignés reçoivent un nom générique (TOUR_PRELIMINAIRE).
     */
    public static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }
}