package com.example.tournament.service.impl;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.MatchMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.MatchGenerationService;
import com.example.tournament.service.MatchService;
import com.example.tournament.util.MatchSpecifications;
import com.example.tournament.util.TournamentStateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchGenerationService matchGenerationService;

    public MatchServiceImpl(MatchRepository matchRepository,
                            TournamentRepository tournamentRepository,
                            MatchGenerationService matchGenerationService) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.matchGenerationService = matchGenerationService;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getById(Long id) {
        return MatchMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Match getEntity(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le match " + id + " n'existe pas."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> listByTournament(Long tournamentId) {
        return matchRepository.findByTournamentIdOrderByRoundNumberAscScheduledAtAsc(tournamentId)
                .stream().map(MatchMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchResponse> search(Long tournamentId, Long teamId, MatchStatus status,
                                      MatchPhase phase, String search, LocalDate from, LocalDate to,
                                      Pageable pageable) {
        return matchRepository.findAll(
                        MatchSpecifications.filter(tournamentId, teamId, status, phase, search, from, to),
                        pageable)
                .map(MatchMapper::toResponse);
    }

    @Override
    @Transactional
    public MatchResponse recordResult(Long matchId, MatchResultRequest request) {
        Match match = getEntity(matchId);
        Tournament tournament = match.getTournament();
        TournamentStateMachine.assertResultOpen(tournament.getStatus());

        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new InvalidOperationException(
                    "Les participants de ce match ne sont pas encore connus.");
        }
        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE
                && request.homeScore().equals(request.awayScore())) {
            throw new InvalidOperationException(
                    "En élimination directe, un vainqueur est obligatoire : le score ne peut pas être nul.");
        }

        match.setHomeScore(request.homeScore());
        match.setAwayScore(request.awayScore());
        match.setStatus(MatchStatus.TERMINE);
        match.setResultRecordedAt(LocalDateTime.now());
        matchRepository.save(match);

        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
            matchGenerationService.advanceAfterRound(tournament, match.getRoundNumber());
        } else {
            long total = matchRepository.countByTournamentId(tournament.getId());
            long finished = matchRepository.countByTournamentIdAndStatus(tournament.getId(),
                    MatchStatus.TERMINE);
            if (total > 0 && total == finished) {
                tournament.setStatus(TournamentStatus.TERMINE);
                tournamentRepository.save(tournament);
            }
        }
        return MatchMapper.toResponse(match);
    }
}