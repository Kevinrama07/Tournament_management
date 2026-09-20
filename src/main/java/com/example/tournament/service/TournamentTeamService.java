package com.example.tournament.service;

import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.dto.response.TournamentTeamResponse;

import java.util.List;

public interface TournamentTeamService {

    List<TournamentTeamResponse> listRegistrations(Long tournamentId);

    TournamentTeamResponse register(Long tournamentId, Long teamId);

    void unregister(Long tournamentId, Long teamId);

    List<TeamResponse> availableTeams(Long tournamentId);
}