package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamRestController {

    private final TeamService teamService;

    public TeamRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public Page<TeamResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TeamStatus status,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return teamService.search(search, status, pageable);
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @GetMapping("/{id}")
    public TeamResponse get(@PathVariable Long id) {
        return teamService.getById(id);
    }

    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/players")
    public List<PlayerResponse> players(@PathVariable Long id) {
        return teamService.players(id);
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<PlayerResponse> addPlayer(@PathVariable Long id,
                                                    @Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.addPlayer(id, request));
    }

    @PutMapping("/{id}/players/{playerId}")
    public PlayerResponse updatePlayer(@PathVariable Long id, @PathVariable Long playerId,
                                       @Valid @RequestBody PlayerRequest request) {
        return teamService.updatePlayer(playerId, request);
    }

    @DeleteMapping("/{id}/players/{playerId}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id, @PathVariable Long playerId) {
        teamService.deletePlayer(playerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public List<TournamentTeamResponse> history(@PathVariable Long id) {
        return teamService.history(id);
    }
}