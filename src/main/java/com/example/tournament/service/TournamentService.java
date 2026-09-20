package com.example.tournament.service;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TournamentService {

    Page<TournamentResponse> search(String search, TournamentStatus status, TournamentType type,
                                    Pageable pageable);

    TournamentResponse getById(Long id);

    Tournament getEntity(Long id);

    TournamentResponse create(TournamentRequest request);

    TournamentResponse update(Long id, TournamentRequest request);

    void delete(Long id);

    TournamentResponse changeStatus(Long id, TournamentStatus newStatus);

    TournamentResponse start(Long id);
}