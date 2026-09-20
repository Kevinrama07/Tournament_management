package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentTeam;
import com.example.tournament.exception.InvalidOperationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentStateMachineTest {

    private Tournament tournament(TournamentStatus status) {
        Tournament t = new Tournament();
        t.setStatus(status);
        t.setName("Tournoi test");
        t.setSport("Football");
        t.setStartDate(LocalDate.now());
        t.setEndDate(LocalDate.now().plusDays(10));
        t.setLocation("Paris");
        t.setMaxTeams(16);
        return t;
    }

    private void register(Tournament t, long teamId) {
        TournamentTeam reg = new TournamentTeam();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Equipe " + teamId);
        team.setCity("Ville");
        reg.setTeam(team);
        t.addRegistration(reg);
    }

    private void addMatch(Tournament t) {
        Match m = new Match();
        m.setScheduledAt(LocalDateTime.now());
        m.setPhase(MatchPhase.REGULIER);
        m.setStatus(MatchStatus.PROGRAMME);
        m.setRoundNumber(1);
        t.addMatch(m);
    }

    @Test
    void brouillon_toInscription_isAllowed() {
        Tournament t = tournament(TournamentStatus.BROUILLON);
        assertThatCode(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.BROUILLON, TournamentStatus.INSCRIPTION, t))
                .doesNotThrowAnyException();
    }

    @Test
    void brouillon_toTermine_isForbidden() {
        Tournament t = tournament(TournamentStatus.BROUILLON);
        assertThatThrownBy(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.BROUILLON, TournamentStatus.TERMINE, t))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void termine_cannotTransitionToAnything() {
        Tournament t = tournament(TournamentStatus.TERMINE);
        assertThatThrownBy(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.TERMINE, TournamentStatus.PROGRAMME, t))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void inscription_toProgramme_requiresAtLeastTwoTeams() {
        Tournament t = tournament(TournamentStatus.INSCRIPTION);
        register(t, 1);
        assertThatThrownBy(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.INSCRIPTION, TournamentStatus.PROGRAMME, t))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void inscription_toProgramme_isAllowedWithTwoTeams() {
        Tournament t = tournament(TournamentStatus.INSCRIPTION);
        register(t, 1);
        register(t, 2);
        assertThatCode(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.INSCRIPTION, TournamentStatus.PROGRAMME, t))
                .doesNotThrowAnyException();
    }

    @Test
    void programme_toEnCours_requiresGeneratedMatches() {
        Tournament t = tournament(TournamentStatus.PROGRAMME);
        assertThatThrownBy(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.PROGRAMME, TournamentStatus.EN_COURS, t))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void programme_toEnCours_isAllowedWithMatches() {
        Tournament t = tournament(TournamentStatus.PROGRAMME);
        addMatch(t);
        assertThatCode(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.PROGRAMME, TournamentStatus.EN_COURS, t))
                .doesNotThrowAnyException();
    }

    @Test
    void enCours_toAnnule_isForbiddenWhenResultRecorded() {
        Tournament t = tournament(TournamentStatus.EN_COURS);
        Match m = new Match();
        m.setHomeScore(2);
        m.setAwayScore(1);
        t.addMatch(m);
        assertThatThrownBy(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.EN_COURS, TournamentStatus.ANNULE, t))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void enCours_toAnnule_isAllowedWithoutResults() {
        Tournament t = tournament(TournamentStatus.EN_COURS);
        addMatch(t);
        assertThatCode(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.EN_COURS, TournamentStatus.ANNULE, t))
                .doesNotThrowAnyException();
    }

    @Test
    void sameStatus_isAlwaysAllowed() {
        Tournament t = tournament(TournamentStatus.EN_COURS);
        assertThatCode(() -> TournamentStateMachine.assertTransitionAllowed(
                TournamentStatus.EN_COURS, TournamentStatus.EN_COURS, t))
                .doesNotThrowAnyException();
    }

    @Test
    void registration_closesOnceStarted() {
        assertThatCode(() -> TournamentStateMachine.assertRegistrationOpen(TournamentStatus.INSCRIPTION))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> TournamentStateMachine.assertRegistrationOpen(TournamentStatus.EN_COURS))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> TournamentStateMachine.assertRegistrationOpen(TournamentStatus.TERMINE))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void unregistration_allowedBeforeStart() {
        assertThatCode(() -> TournamentStateMachine.assertUnregistrationOpen(TournamentStatus.PROGRAMME))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> TournamentStateMachine.assertUnregistrationOpen(TournamentStatus.EN_COURS))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void generation_blockedForDraft() {
        assertThatThrownBy(() -> TournamentStateMachine.assertGenerationOpen(TournamentStatus.BROUILLON))
                .isInstanceOf(InvalidOperationException.class);
        assertThatCode(() -> TournamentStateMachine.assertGenerationOpen(TournamentStatus.PROGRAMME))
                .doesNotThrowAnyException();
    }

    @Test
    void result_blockedWhenNoLongerInProgress() {
        assertThatCode(() -> TournamentStateMachine.assertResultOpen(TournamentStatus.EN_COURS))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> TournamentStateMachine.assertResultOpen(TournamentStatus.TERMINE))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> TournamentStateMachine.assertResultOpen(TournamentStatus.INSCRIPTION))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void modification_blockedForFinishedTournaments() {
        assertThatThrownBy(() -> TournamentStateMachine.assertModifiable(TournamentStatus.TERMINE))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> TournamentStateMachine.assertModifiable(TournamentStatus.ANNULE))
                .isInstanceOf(InvalidOperationException.class);
        assertThatCode(() -> TournamentStateMachine.assertModifiable(TournamentStatus.EN_COURS))
                .doesNotThrowAnyException();
    }
}