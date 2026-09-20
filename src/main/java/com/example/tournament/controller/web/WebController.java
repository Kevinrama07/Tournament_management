package com.example.tournament.controller.web;

import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.TournamentsStatsResponse;
import com.example.tournament.service.DashboardService;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.TournamentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    private final DashboardService dashboardService;
    private final TournamentService tournamentService;
    private final MatchService matchService;

    public WebController(DashboardService dashboardService,
                         TournamentService tournamentService,
                         MatchService matchService) {
        this.dashboardService = dashboardService;
        this.tournamentService = tournamentService;
        this.matchService = matchService;
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Identifiants invalides.");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté.");
        }
        return "login";
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        TournamentsStatsResponse stats = dashboardService.globalStats();
        model.addAttribute("stats", stats);
        model.addAttribute("latestTournaments",
                tournamentService.search(null, null, null,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
        Page<MatchResponse> recentMatches = matchService.search(null, null, null, null, null, null,
                null, PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "scheduledAt")));
        model.addAttribute("recentMatches", recentMatches.getContent());
        if (authentication != null) {
            model.addAttribute("displayName", authentication.getName());
        }
        return "dashboard";
    }

    @GetMapping("/error/business")
    public String businessError() {
        return "error-business";
    }
}