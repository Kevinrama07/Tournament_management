package com.example.tournament.service;

import com.example.tournament.dto.response.BracketTreeView;
import com.example.tournament.dto.response.RankingResponse;
import com.example.tournament.dto.response.TournamentStatsResponse;
import com.example.tournament.entity.RankingCriterion;

import java.util.List;

public interface RankingAndBracketService {

    RankingResponse ranking(Long tournamentId, List<RankingCriterion> criteria);

    BracketTreeView bracket(Long tournamentId);

    TournamentStatsResponse stats(Long tournamentId);
}