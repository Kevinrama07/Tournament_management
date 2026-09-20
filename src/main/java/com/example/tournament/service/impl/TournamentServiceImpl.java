package com.example.tournament.service.impl;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.TournamentMapper;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.TournamentService;
import com.example.tournament.util.TournamentSpecifications;
import com.example.tournament.util.TournamentStateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentServiceImpl(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TournamentResponse> search(String search, TournamentStatus status,
                                           TournamentType type, Pageable pageable) {
        return tournamentRepository.findAll(TournamentSpecifications.filter(search, status, type),
                        pageable)
                .map(TournamentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResponse getById(Long id) {
        return TournamentMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Tournament getEntity(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + id + " n'existe pas."));
    }

    @Override
    @Transactional
    public TournamentResponse create(TournamentRequest request) {
        validateDates(request);
        if (tournamentRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Un tournoi nommé « " + request.name() + " » existe déjà.");
        }
        Tournament tournament = TournamentMapper.toEntity(request);
        tournament.setStatus(TournamentStatus.BROUILLON);
        return TournamentMapper.toResponse(tournamentRepository.save(tournament));
    }

    @Override
    @Transactional
    public TournamentResponse update(Long id, TournamentRequest request) {
        Tournament tournament = getEntity(id);
        TournamentStateMachine.assertModifiable(tournament.getStatus());
        validateDates(request);
        if (!tournament.getName().equalsIgnoreCase(request.name())
                && tournamentRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Un tournoi nommé « " + request.name() + " » existe déjà.");
        }
        return TournamentMapper.toResponse(tournamentRepository.save(
                TournamentMapper.toEntity(request, tournament)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tournament tournament = getEntity(id);
        if (tournament.getStatus() != TournamentStatus.BROUILLON
                && tournament.getStatus() != TournamentStatus.ANNULE) {
            throw new InvalidOperationException(
                    "Seul un tournoi en brouillon ou annulé peut être supprimé (statut actuel : "
                            + tournament.getStatus() + ").");
        }
        tournamentRepository.delete(tournament);
    }

    @Override
    @Transactional
    public TournamentResponse changeStatus(Long id, TournamentStatus newStatus) {
        Tournament tournament = getEntity(id);
        TournamentStateMachine.assertTransitionAllowed(tournament.getStatus(), newStatus, tournament);
        tournament.setStatus(newStatus);
        return TournamentMapper.toResponse(tournamentRepository.save(tournament));
    }

    @Override
    @Transactional
    public TournamentResponse start(Long id) {
        return changeStatus(id, TournamentStatus.EN_COURS);
    }

    private void validateDates(TournamentRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidOperationException(
                    "La date de fin doit être postérieure à la date de début.");
        }
    }
}