package com.example.tournament.service.impl;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.Player;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.MatchMapper;
import com.example.tournament.mapper.PlayerMapper;
import com.example.tournament.mapper.TeamMapper;
import com.example.tournament.repository.PlayerRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentTeamRepository;
import com.example.tournament.service.TeamService;
import com.example.tournament.util.TeamSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           PlayerRepository playerRepository,
                           TournamentTeamRepository tournamentTeamRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> search(String search, TeamStatus status, Pageable pageable) {
        return teamRepository.findAll(TeamSpecifications.filter(search, status), pageable)
                .map(TeamMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        return TeamMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Team getEntity(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "L'équipe " + id + " n'existe pas."));
    }

    @Override
    @Transactional
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Une équipe nommée « " + request.name() + " » existe déjà.");
        }
        Team team = TeamMapper.toEntity(request);
        return TeamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = getEntity(id);
        if (!team.getName().equalsIgnoreCase(request.name())
                && teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Une équipe nommée « " + request.name() + " » existe déjà.");
        }
        return TeamMapper.toResponse(teamRepository.save(TeamMapper.toEntity(request, team)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Team team = getEntity(id);
        boolean stillCompeting = team.getTournaments().stream()
                .anyMatch(t -> {
                    TournamentStatus s = t.getTournament().getStatus();
                    return s != TournamentStatus.TERMINE && s != TournamentStatus.ANNULE;
                });
        if (stillCompeting) {
            throw new ConflictException(
                    "Impossible de supprimer l'équipe : elle participe encore à un tournoi actif.");
        }
        teamRepository.delete(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> players(Long teamId) {
        getEntity(teamId);
        return playerRepository.findByTeamIdOrderByShirtNumberAsc(teamId)
                .stream().map(PlayerMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PlayerResponse addPlayer(Long teamId, PlayerRequest request) {
        Team team = getEntity(teamId);
        Player player = PlayerMapper.toEntity(request);
        team.addPlayer(player);
        return PlayerMapper.toResponse(playerRepository.save(player));
    }

    @Override
    @Transactional
    public PlayerResponse updatePlayer(Long playerId, PlayerRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le joueur " + playerId + " n'existe pas."));
        return PlayerMapper.toResponse(playerRepository.save(PlayerMapper.toEntity(request, player)));
    }

    @Override
    @Transactional
    public void deletePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le joueur " + playerId + " n'existe pas."));
        if (player.getTeam() != null) {
            player.getTeam().removePlayer(player);
        }
        playerRepository.delete(player);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentTeamResponse> history(Long teamId) {
        getEntity(teamId);
        return tournamentTeamRepository.findByTeamIdOrderByRegistrationDateDesc(teamId)
                .stream().map(MatchMapper::toResponse).toList();
    }
}