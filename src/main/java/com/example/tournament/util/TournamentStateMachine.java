package com.example.tournament.util;

import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.exception.InvalidOperationException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Machine à états du tournoi : définit les transitions de statut autorisées
 * et les règles d'accès aux opérations (inscription, génération, saisie de résultat...).
 */
public final class TournamentStateMachine {

    private TournamentStateMachine() {
    }

    private static final Set<TournamentStatus> REGISTRATION_OPEN = EnumSet.of(
            TournamentStatus.BROUILLON, TournamentStatus.INSCRIPTION);

    private static final Set<TournamentStatus> UNREGISTRATION_OPEN = EnumSet.of(
            TournamentStatus.BROUILLON, TournamentStatus.INSCRIPTION, TournamentStatus.PROGRAMME);

    private static final Set<TournamentStatus> GENERATION_OPEN = EnumSet.of(
            TournamentStatus.INSCRIPTION, TournamentStatus.PROGRAMME, TournamentStatus.EN_COURS);

    private static final Set<TournamentStatus> RESULT_OPEN = EnumSet.of(
            TournamentStatus.PROGRAMME, TournamentStatus.EN_COURS);

    public static void assertTransitionAllowed(TournamentStatus from, TournamentStatus to,
                                               Tournament tournament) {
        if (from == to) {
            return;
        }
        boolean allowed = switch (from) {
            case BROUILLON -> to == TournamentStatus.INSCRIPTION
                    || to == TournamentStatus.PROGRAMME || to == TournamentStatus.ANNULE;
            case INSCRIPTION -> to == TournamentStatus.PROGRAMME || to == TournamentStatus.ANNULE;
            case PROGRAMME -> to == TournamentStatus.EN_COURS || to == TournamentStatus.ANNULE;
            case EN_COURS -> to == TournamentStatus.ANNULE
                    && tournament.getMatches().stream()
                        .noneMatch(m -> m.getHomeScore() != null || m.getAwayScore() != null);
            case TERMINE, ANNULE -> false;
        };
        if (!allowed) {
            throw new InvalidOperationException(
                    "Transition de statut impossible : " + from + " → " + to + ".");
        }
        if (to == TournamentStatus.PROGRAMME
                && tournament.getRegistrations().size() < 2) {
            throw new InvalidOperationException(
                    "Impossible de programmer le tournoi : il faut au moins 2 équipes inscrites.");
        }
        if (to == TournamentStatus.EN_COURS && tournament.getMatches().isEmpty()) {
            throw new InvalidOperationException(
                    "Impossible de démarrer le tournoi : le calendrier n'a pas été généré.");
        }
    }

    public static void assertModifiable(TournamentStatus status) {
        if (status == TournamentStatus.TERMINE || status == TournamentStatus.ANNULE) {
            throw new InvalidOperationException(
                    "Un tournoi " + status + " ne peut plus être modifié.");
        }
    }

    public static void assertRegistrationOpen(TournamentStatus status) {
        if (!REGISTRATION_OPEN.contains(status)) {
            throw new InvalidOperationException(
                    "L'inscription d'équipes n'est plus possible : le tournoi est "
                            + status + ".");
        }
    }

    public static void assertUnregistrationOpen(TournamentStatus status) {
        if (!UNREGISTRATION_OPEN.contains(status)) {
            throw new InvalidOperationException(
                    "Une équipe ne peut plus être retirée : le tournoi est " + status + ".");
        }
    }

    public static void assertGenerationOpen(TournamentStatus status) {
        if (!GENERATION_OPEN.contains(status)) {
            throw new InvalidOperationException(
                    "La génération du calendrier n'est pas possible quand le tournoi est "
                            + status + ".");
        }
    }

    public static void assertResultOpen(TournamentStatus status) {
        if (!RESULT_OPEN.contains(status)) {
            throw new InvalidOperationException(
                    "Un résultat ne peut être saisi quand le tournoi est " + status + ".");
        }
    }
}