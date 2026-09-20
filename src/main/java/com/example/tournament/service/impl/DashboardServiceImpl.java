package com.example.tournament.service.impl;

import com.example.tournament.dto.response.TournamentsStatsResponse;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.repository.PlayerRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.TournamentTeamRepository;
import com.example.tournament.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    public DashboardServiceImpl(TournamentRepository tournamentRepository,
                                TeamRepository teamRepository,
                                PlayerRepository playerRepository,
                                TournamentTeamRepository tournamentTeamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentsStatsResponse globalStats() {
        long totalTournaments = tournamentRepository.count();
        long active = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.EN_COURS).count();
        long completed = tournamentRepository.findAll().stream()
                .filter(t -> t.getStatus() == TournamentStatus.TERMINE).count();
        return new TournamentsStatsResponse(
                totalTournaments,
                teamRepository.count(),
                playerRepository.count(),
                active,
                completed,
                tournamentTeamRepository.count()
        );
    }
}