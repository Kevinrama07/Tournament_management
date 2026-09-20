package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/matches")
public class MatchRestController {

    private final MatchService matchService;

    public MatchRestController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public Page<MatchResponse> list(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) MatchPhase phase,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "scheduledAt", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return matchService.search(tournamentId, teamId, status, phase, search, from, to, pageable);
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return matchService.getById(id);
    }

    @PutMapping("/{id}/result")
    public MatchResponse recordResult(@PathVariable Long id,
                                      @Valid @RequestBody MatchResultRequest request) {
        return matchService.recordResult(id, request);
    }
}