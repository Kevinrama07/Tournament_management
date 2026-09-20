package com.example.tournament.repository;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    List<Match> findByTournamentIdOrderByRoundNumberAscScheduledAtAsc(Long tournamentId);

    List<Match> findByTournamentIdAndRoundNumberOrderByIdAsc(Long tournamentId, int roundNumber);

    long countByTournamentId(Long tournamentId);

    long countByTournamentIdAndStatus(Long tournamentId, MatchStatus status);

    boolean existsByTournamentId(Long tournamentId);

    List<Match> findByTournamentIdAndStatusAndRoundNumber(Long tournamentId, MatchStatus status, int roundNumber);

    boolean existsByTournamentIdAndRoundNumber(Long tournamentId, int roundNumber);
}