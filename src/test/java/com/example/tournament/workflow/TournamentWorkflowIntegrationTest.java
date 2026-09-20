package com.example.tournament.workflow;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.RankingResponse;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.service.MatchGenerationService;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingAndBracketService;
import com.example.tournament.service.TeamService;
import com.example.tournament.service.TournamentService;
import com.example.tournament.service.TournamentTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.demo-data.enabled=false"
})
@Transactional
class TournamentWorkflowIntegrationTest {

    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TournamentTeamService tournamentTeamService;
    @Autowired
    private MatchGenerationService matchGenerationService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private RankingAndBracketService rankingAndBracketService;

    private Long createTeam(String name) {
        return teamService.create(new TeamRequest(name, null, "Paris", "Coach")).id();
    }

    private TournamentResponse createChampionnat() {
        return tournamentService.create(new TournamentRequest(
                "Championnat Test " + System.nanoTime(), "Football", "Test", LocalDate.now(),
                LocalDate.now().plusMonths(1), "Stade", 8, TournamentType.CHAMPIONNAT_SIMPLE));
    }

    private void registerTeams(Long tournamentId, String... names) {
        for (String name : names) {
            tournamentTeamService.register(tournamentId, createTeam(name));
        }
    }

    @Test
    void fullChampionnatWorkflow_generatesMatches_andComputesRanking() {
        TournamentResponse tournament = createChampionnat();
        Long tournamentId = tournament.id();
        registerTeams(tournamentId, "Alpha", "Bravo", "Charlie", "Delta");

        tournamentService.changeStatus(tournamentId, TournamentStatus.INSCRIPTION);
        tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);
        matchGenerationService.generate(tournamentId);

        // Championnat simple à 4 équipes → 6 matchs (3 journées × 2)
        List<MatchResponse> matches = matchService.listByTournament(tournamentId);
        assertThat(matches).hasSize(6);
        assertThat(matches).extracting(MatchResponse::homeTeamId, MatchResponse::awayTeamId)
                .doesNotHaveDuplicates();

        // Saisie de tous les résultats
        for (MatchResponse m : matches) {
            matchService.recordResult(m.id(), new MatchResultRequest(
                    m.roundNumber() % 2 == 0 ? 2 : 1,
                    m.roundNumber() % 2 == 0 ? 0 : 1));
        }

        // le championnat se termine automatiquement une fois tous les matchs joués
        assertThat(tournamentService.getById(tournamentId).status())
                .isEqualTo(TournamentStatus.TERMINE);

        // Classement calculé et trié par points décroissants
        RankingResponse ranking = rankingAndBracketService.ranking(tournamentId, null);
        assertThat(ranking.standings()).hasSize(4);
        assertThat(ranking.standings()).extracting(s -> s.position())
                .containsExactly(1, 2, 3, 4);
        assertThat(ranking.standings().stream().mapToInt(s -> s.points()).toArray())
                .isSortedAccordingTo((a, b) -> Integer.compare((Integer) b, (Integer) a));

        long played = ranking.standings().stream().filter(s -> s.played() == 3).count();
        assertThat(played).isEqualTo(4);
    }

    @Test
    void duplicateTeamRegistration_isRejected() {
        Long tournamentId = createChampionnat().id();
        Long teamId = createTeam("Solo");

        tournamentTeamService.register(tournamentId, teamId);
        assertThatThrownBy(() -> tournamentTeamService.register(tournamentId, teamId))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void doubleMatchGeneration_isPrevented() {
        Long tournamentId = createChampionnat().id();
        registerTeams(tournamentId, "A", "B", "C", "D");
        tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);
        matchGenerationService.generate(tournamentId);

        assertThatThrownBy(() -> matchGenerationService.generate(tournamentId))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotStartTournamentWithoutMatches() {
        Long tournamentId = createChampionnat().id();
        registerTeams(tournamentId, "E", "F");
        tournamentService.changeStatus(tournamentId, TournamentStatus.INSCRIPTION);
        tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);

        assertThatThrownBy(() -> tournamentService.start(tournamentId))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void knockoutFlow_advancesToFinals_andTerminates() {
        Long tournamentId = tournamentService.create(new TournamentRequest(
                "Coupe Test " + System.nanoTime(), "Football", "Coupe", LocalDate.now(),
                LocalDate.now().plusMonths(1), "Stade", 8, TournamentType.ELIMINATION_DIRECTE))
                .id();
        // 3 équipes → tableau de 4 : une équipe est exempte, 1 match au 1er tour
        registerTeams(tournamentId, "X1", "X2", "X3");
        tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);
        matchGenerationService.generate(tournamentId);

        List<MatchResponse> matches = matchService.listByTournament(tournamentId);
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).roundNumber()).isEqualTo(1);

        // Victoire du premier tour → la finale est générée
        matchService.recordResult(matches.get(0).id(), new MatchResultRequest(1, 0));
        List<MatchResponse> next = matchService.listByTournament(tournamentId);
        assertThat(next).hasSize(2);
        MatchResponse finale = next.stream().filter(m -> m.roundNumber() == 2).findFirst().orElseThrow();

        // La finale se joue puis le tournoi se termine
        matchService.recordResult(finale.id(), new MatchResultRequest(2, 1));
        assertThat(tournamentService.getById(tournamentId).status())
                .isEqualTo(TournamentStatus.TERMINE);

        MatchResponse winner = matchService.getById(finale.id());
        assertThat(winner.winnerTeamId()).isNotNull();
    }

    @Test
    void knockoutDrawScore_isRejected() {
        Long tournamentId = tournamentService.create(new TournamentRequest(
                "Coupe Draw Test " + System.nanoTime(), "Football", "Coupe", LocalDate.now(),
                LocalDate.now().plusMonths(1), "Stade", 8, TournamentType.ELIMINATION_DIRECTE))
                .id();
        registerTeams(tournamentId, "Y1", "Y2", "Y3");
        tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);
        matchGenerationService.generate(tournamentId);

        MatchResponse first = matchService.listByTournament(tournamentId).get(0);
        assertThatThrownBy(() -> matchService.recordResult(first.id(), new MatchResultRequest(1, 1)))
                .isInstanceOf(InvalidOperationException.class);
    }
}