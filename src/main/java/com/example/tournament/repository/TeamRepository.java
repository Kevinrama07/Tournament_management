package com.example.tournament.repository;

import com.example.tournament.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    boolean existsByNameIgnoreCase(String name);

    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);
}