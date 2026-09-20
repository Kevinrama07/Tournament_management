package com.example.tournament.service;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface MatchService {

    MatchResponse getById(Long id);

    List<MatchResponse> listByTournament(Long tournamentId);

    Page<MatchResponse> search(Long tournamentId, Long teamId, MatchStatus status,
                               MatchPhase phase, String search, LocalDate from, LocalDate to,
                               Pageable pageable);

    MatchResponse recordResult(Long matchId, MatchResultRequest request);
}