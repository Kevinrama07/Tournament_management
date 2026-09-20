package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.request.TournamentStatusRequest;
import com.example.tournament.dto.request.TournamentTeamRequest;
import com.example.tournament.dto.response.BracketTreeView;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.RankingResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.dto.response.TournamentStatsResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.RankingCriterion;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.service.MatchGenerationService;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingAndBracketService;
import com.example.tournament.service.TournamentService;
import com.example.tournament.service.TournamentTeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentRestController {

    private final TournamentService tournamentService;
    private final TournamentTeamService tournamentTeamService;
    private final MatchGenerationService matchGenerationService;
    private final MatchService matchService;
    private final RankingAndBracketService rankingAndBracketService;

    public TournamentRestController(TournamentService tournamentService,
                                    TournamentTeamService tournamentTeamService,
                                    MatchGenerationService matchGenerationService,
                                    MatchService matchService,
                                    RankingAndBracketService rankingAndBracketService) {
        this.tournamentService = tournamentService;
        this.tournamentTeamService = tournamentTeamService;
        this.matchGenerationService = matchGenerationService;
        this.matchService = matchService;
        this.rankingAndBracketService = rankingAndBracketService;
    }

    @GetMapping
    public Page<TournamentResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TournamentStatus status,
            @RequestParam(required = false) TournamentType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return tournamentService.search(search, status, type, pageable);
    }

    @PostMapping
    public ResponseEntity<TournamentResponse> create(
            @Valid @RequestBody TournamentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.create(request));
    }

    @GetMapping("/{id}")
    public TournamentResponse get(@PathVariable Long id) {
        return tournamentService.getById(id);
    }

    @PutMapping("/{id}")
    public TournamentResponse update(@PathVariable Long id,
                                     @Valid @RequestBody TournamentRequest request) {
        return tournamentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public TournamentResponse changeStatus(@PathVariable Long id,
                                           @Valid @RequestBody TournamentStatusRequest request) {
        return tournamentService.changeStatus(id, request.status());
    }

    @PostMapping("/{id}/start")
    public TournamentResponse start(@PathVariable Long id) {
        return tournamentService.start(id);
    }

    @GetMapping("/{id}/teams")
    public List<TournamentTeamResponse> registrations(@PathVariable Long id) {
        return tournamentTeamService.listRegistrations(id);
    }

    @GetMapping("/{id}/available-teams")
    public List<TeamResponse> availableTeams(@PathVariable Long id) {
        return tournamentTeamService.availableTeams(id);
    }

    @PostMapping("/{id}/teams")
    public ResponseEntity<TournamentTeamResponse> register(@PathVariable Long id,
                                                           @Valid @RequestBody TournamentTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentTeamService.register(id, request.teamId()));
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Void> unregister(@PathVariable Long id, @PathVariable Long teamId) {
        tournamentTeamService.unregister(id, teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-matches")
    public ResponseEntity<TournamentResponse> generateMatches(@PathVariable Long id) {
        matchGenerationService.generate(id);
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @GetMapping("/{id}/matches")
    public List<MatchResponse> matches(@PathVariable Long id) {
        return matchService.listByTournament(id);
    }

    @GetMapping("/{id}/ranking")
    public RankingResponse ranking(@PathVariable Long id,
                                   @RequestParam(required = false) List<RankingCriterion> criteria) {
        return rankingAndBracketService.ranking(id, criteria);
    }

    @GetMapping("/{id}/bracket")
    public BracketTreeView bracket(@PathVariable Long id) {
        return rankingAndBracketService.bracket(id);
    }

    @GetMapping("/{id}/stats")
    public TournamentStatsResponse stats(@PathVariable Long id) {
        return rankingAndBracketService.stats(id);
    }
}