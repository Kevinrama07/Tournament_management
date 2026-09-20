package com.example.tournament.repository;

import com.example.tournament.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamIdOrderByShirtNumberAsc(Long teamId);
}