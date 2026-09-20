package com.example.tournament.service;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {

    Page<TeamResponse> search(String search, TeamStatus status, Pageable pageable);

    TeamResponse getById(Long id);

    Team getEntity(Long id);

    TeamResponse create(TeamRequest request);

    TeamResponse update(Long id, TeamRequest request);

    void delete(Long id);

    List<PlayerResponse> players(Long teamId);

    PlayerResponse addPlayer(Long teamId, PlayerRequest request);

    PlayerResponse updatePlayer(Long playerId, PlayerRequest request);

    void deletePlayer(Long playerId);

    List<TournamentTeamResponse> history(Long teamId);
}