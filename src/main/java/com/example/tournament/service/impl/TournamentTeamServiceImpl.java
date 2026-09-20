package com.example.tournament.service.impl;

import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentTeam;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.MatchMapper;
import com.example.tournament.mapper.TeamMapper;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentTeamRepository;
import com.example.tournament.service.TournamentTeamService;
import com.example.tournament.util.TournamentStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TournamentTeamServiceImpl implements TournamentTeamService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    public TournamentTeamServiceImpl(TournamentRepository tournamentRepository,
                                     TeamRepository teamRepository,
                                     TournamentTeamRepository tournamentTeamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentTeamResponse> listRegistrations(Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        return tournamentTeamRepository.findByTournamentIdOrderBySeedAsc(tournamentId)
                .stream().map(MatchMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TournamentTeamResponse register(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        TournamentStateMachine.assertRegistrationOpen(tournament.getStatus());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "L'équipe " + teamId + " n'existe pas."));

        if (team.getStatus() != TeamStatus.ACTIF) {
            throw new ConflictException(
                    "L'équipe « " + team.getName() + " » n'est pas active : inscription refusée.");
        }
        if (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournamentId, teamId)) {
            throw new ConflictException("L'équipe « " + team.getName()
                    + " » est déjà inscrite à ce tournoi.");
        }
        long current = tournamentTeamRepository.countByTournamentId(tournamentId);
        if (current >= tournament.getMaxTeams()) {
            throw new ConflictException("Le tournoi a atteint son nombre maximum d'équipes ("
                    + tournament.getMaxTeams() + ").");
        }

        TournamentTeam registration = new TournamentTeam();
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setSeed((int) current + 1);
        tournament.addRegistration(registration);
        registration.setTeam(team);

        return MatchMapper.toResponse(tournamentTeamRepository.save(registration));
    }

    @Override
    @Transactional
    public void unregister(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le tournoi " + tournamentId + " n'existe pas."));
        TournamentStateMachine.assertUnregistrationOpen(tournament.getStatus());

        TournamentTeam registration = tournamentTeamRepository
                .findByTournamentIdAndTeamId(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "L'équipe " + teamId + " n'est pas inscrite à ce tournoi."));
        tournamentTeamRepository.delete(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> availableTeams(Long tournamentId) {
        List<Long> registeredIds = tournamentTeamRepository
                .findByTournamentIdOrderBySeedAsc(tournamentId)
                .stream().map(t -> t.getTeam().getId()).toList();
        return teamRepository.findAll().stream()
                .filter(t -> t.getStatus() == TeamStatus.ACTIF)
                .filter(t -> !registeredIds.contains(t.getId()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(TeamMapper::toResponse)
                .toList();
    }
}