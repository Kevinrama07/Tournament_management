package com.example.tournament.repository;

import com.example.tournament.entity.TournamentTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

    List<TournamentTeam> findByTournamentIdOrderBySeedAsc(Long tournamentId);

    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    long countByTournamentId(Long tournamentId);

    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    Page<TournamentTeam> findByTournamentId(Long tournamentId, Pageable pageable);

    List<TournamentTeam> findByTeamIdOrderByRegistrationDateDesc(Long teamId);
}